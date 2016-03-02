<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="resources/css/bootstrap.css" />
    <link rel="stylesheet" href="resources/jstree/themes/default/style.css" />
    <script src="resources/js/jquery.js"></script>
    <script src="resources/js/bootstrap.js"></script>
    <script src="resources/jstree/jstree.js"></script>
    <style>
    .left-panel,
    .right-panel {
        min-height: 100%;
        min-width: 100px;
        box-sizing: normal;
        padding-top: 0px;
        padding-bottom: 0px;
        margin-bottom: -2px;
        margin-top: -2px;
    }
    
    .left-panel {
        position: absolute;
        top: 0;
        left: 0;
        width: 100px;
        overflow: auto;
    }
    
    .right-panel {
        position: absolute;
        top: 0px;
        left: 100px;
        right: 0px;
        padding-top: 10px;
    }
    
    .path-banner {
        background-color: #fff;
    }
    
    .text-content {
        width: 100%;
        margin-top: 10px;
    }
    </style>
</head>

<body>
    <div class="container">
        <div class="well left-panel">
            <div id="tree">
            </div>
        </div>
        <div class="well right-panel">
            <button type="button" class="btn btn-default" id="update-btn">Update</button>
            <div id="content-detail">
            </div>
        </div>
    </div>
    <iframe style="display:none" name="downloadFile"></iframe>
</body>
<script>
var Utils = {
    extend: function(target, src) {
        if (!src) {
            return target;
        }
        for (var p in src) {
            target[p] = src[p];
        }
        return target;
    },
    replace: function(template,replacement){
    	return template.replace(/\{\{([^\{\{\}\}]+)\}\}/g,function(m,g1){
			return replacement[g1] || "";
		});
    },
    isString: function(s){
    	return typeof(s) === "string";
    }
};
var TemplateCache = {
	_cache: {},	
	getTemplate: function(url){
		var cache = this._cache,
			defer = $.Deferred();
		if(cache[url]){
			defer.resolve(cache[url]);
		}else{
			$.get(url).then(function(res){
				cache[url] = res;
				defer.resolve(res);
			});
		}
		return defer.promise();
	}
};
function ModalPopup(opts){
	var _this = this;
	TemplateCache.getTemplate("html/modalPopup.html").then(function(template){
		_this.template = template;
		_this.init(opts);
	});
}
ModalPopup.prototype = {
	init: function(opts){
		var default_opts = {
			title: "",
			content: "",
			isModal: true,
			buttons: []//{text,onclick,cls}
		};
		var _opts = Utils.extend(default_opts, opts);
		var buttonsHTML = [],
			callBack = {};
		for(var i=0,len=_opts.buttons.length;i<len;i++){
			var btn = _opts.buttons[i];
			callBack[btn.text] = btn.onclick;
			buttonsHTML.push(Utils.replace('<button type="button" name="{{text}}" class="btn {{cls}}">{{text}}</button>',btn));
		}
		_opts.buttonsHTML = buttonsHTML.join("");
		var template = Utils.replace(this.template,_opts);
		var _body = $(template);
		_body.appendTo(document.body);
		for(var p in callBack){
			_body.find("[name="+p+"]").on("click",callBack[p]);
		}
		this._body = _body;
		
		var events = ["click","dbclick","mouseover"];
	    var _this = this;
	    for(var i=0,len=events.length;i<len;i++){
	    	var ele = _body.find("[_"+events[i]+"]");
	    	if(ele.length){
	    		for(var j=0,l=ele.length;j<l;j++){
	    			$(ele[j]).on(events[i],generate(events[i]));
	    		}
	    	}
	    }
	    function generate(evtName){
			return function(){
				var fn = $(this).attr("_"+evtName).replace("()","");
				_this[fn]();
			};
		}
	},
	_run: function(method){
		if(this._body){
			this._body.modal(method);
		}else{
			var fn = this[method];
			var _this = this;
			setTimeout(function(){
				fn.call(_this);
			},100);
		}
	},
	show: function(){
		this._run("show");
	},
	hide: function(){
		this._run("hide");
	},
	close: function(){
		this._run("removeBackdrop");
		this._body.remove();
		for(var p in this){
			delete this[p];
		}
	}
}

function Message(opts) {
    var defaults = {
        title: "",
        content: "",
        btns: []
    }
    var _this = this;
    var buttons = {
    	close: {
    		text: "Close",
            cls: "btn-default",
            onclick: function() {
                _this.close();
            }
    	}
    };
    
    var _opts = Utils.extend(defaults, opts);
    for(var i=0,len=_opts.buttons.length;i<len;i++){
    	if(Utils.isString(_opts.buttons[i]) && buttons[_opts.buttons[i]]){
    		_opts.buttons[i] = buttons[_opts.buttons[i]];
    	}
    }
    var popup = new ModalPopup(_opts);
    this.popup = popup;
}
Message.prototype = {
    _createModalBack: function() {
        return $("<div class='modal-backdrop fade'>");
    },
    addButtons: function() {
        var _this = this;
        for (var i = 0, len = _opts.btns.length; i < len; i++) {
            var cfg = _opts.btns[i];
            var $btn = $("<button type='button' class='btn'>" + cfg.text + "</button>");
            $btn.on("click", function() {
                cfg.callBack.apply(_this);
            });
        }
    },
    setTitle: function(title) {
        this.$title.html(title);
    },
    setContent: function(cnt) {
        this.$body.html(cnt);
    },
    show: function() {
        this.popup.show();
    },
    hide: function() {
    	this.popup.hide();
    },
    close: function() {
        this.popup.close();
    }
}
var MsgUtils = {
    alert: function(cnt) {
    	var msg = new Message({
    		title: "Alert",
            content: cnt,
    	});
    	msg.show();
    },
    confirm: function(cnt,callBack){
    	var msg = new Message({
    		title: "Confirm",
            content: cnt,
            buttons:["close",{
        		text: "ok",
                cls: "btn-default",
                onclick: function() {
                	callBack && callBack();
                	msg.close();
                }
        	}]
    	});
    	msg.show();
    },
    prompt: function(cnt, cb) {
    	
    }
};
function ContextMenu(menuItems) {
    var dom = document.createElement("ul");
    dom.className = "dropdown-menu";
    this.dom = dom;
    if (menuItems) {
        this._createMenu(menuItems);
    }
    document.body.appendChild(this.dom);
    this.dom.style.display = "none";
    var _this = this;
    $("body").on("click",function(){
    	_this.hide();
    });
}
ContextMenu.prototype = {
    show: function(menuItems) {
        if (menuItems) {
            this._createMenu(menuItems);
        }
        this.dom.style.display = "block";
    },
    showAt: function(xy) {
        var style = this.dom.style;
        style.left = xy.x
        style.top = xy.y;
        style.display = "block";
    },
    hide: function() {
        this.dom.style.display = "none";
    },
    setMenuItems: function(menuItems) {
    	this.dom.innerHTML = "";
    	this._createMenu(menuItems);
    },
    _createMenu: function(menuItems) {
        var menu = this;
        for (var i = 0, len = menuItems.length; i < len; i++) {
            var li = document.createElement("li");
            li.innerHTML = "<a href='javascript:void(0)'>" + menuItems[i].text + "</a>";
            li.onclick = (function(item) {
                return function(e) {
                    e.stopImmediatePropagation();
                    item.onclick && item.onclick();
                    menu.hide();
                }
            })(menuItems[i]);
            this.dom.appendChild(li);
        }
    }
}

function PathBanner(container) {
    var dom = document.createElement("ol");
    dom.className = "breadcrumb path-banner";
    this.dom = dom;
    this.container = container;
}
PathBanner.prototype = {
    showPath: function(paths, cb) {
        this.dom.innerHTML = "";
        if (paths && paths.length) {
            for (var i = 0, len = paths.length - 1; i < len; i++) {
                var li = document.createElement("li");
                li.innerHTML = "<a href='javascript:void(0)'>" + paths[i].text + "&nbsp;/&nbsp;</a>";
                li.onclick = (function(path, index) {
                    return function(e) {
                        e.stopImmediatePropagation();
                        cb(path, index);
                    }
                })(paths[i], i);
                this.dom.appendChild(li);
            }
            var li = document.createElement("li");
            li.className = "active";
            li.innerHTML = paths[paths.length - 1].text;
            this.dom.appendChild(li);
        }
        this.container.insertBefore(this.dom, this.container.children[0]);
    },
    destory: function() {
        this.dom.parentNode.removeChild(this.dom);
        this.dom = null;
        delete this.dom;
    }
}
var Context = {
    adjustLayout: function(width) {
        var left = $(".left-panel"),
            right = $(".right-panel");
        left.width(0);
        width = width || left[0].scrollWidth,
            padding_right = parseInt(left.css("padding-right")),
            padding_left = parseInt(left.css("padding-left"));
        left.width(width);
        right.css({
            left: width + padding_left + padding_right + "px"
        });
    },
    ajustHeight: function(container) {
        var $container = $(container);
        $container.height(0);
        $container.height($container[0].scrollHeight);
    },
    ajustWidth: function(container) {
        var $container = $(container);
        $container.width(0);
        $container.width($container[0].scrollWidth);
    }
};

function showFileDetail(nodeFile) {
    var cntContainer = $("#content-detail");
    if (nodeFile.type === "image") {
        cntContainer.html("<img src='data:image/png;base64," + nodeFile.content + "' />");
    } else {
        var o = $("<textarea class='text-content'></textarea>");
        o.val(nodeFile.content);
        cntContainer.html(o);
        Context.ajustHeight(o);
    }
}

function transToJsTreeJson(data) {
    var arr = [],
        fn = arguments.callee;
    for (var i = 0, len = data.length; i < len; i++) {
        var obj = {};
        obj.text = data[i].name;
        obj.data = {
            filePath: data[i].fullName,
            isFolder: data[i].folder
        };
        if (data[i].children && data[i].children.length) {
            obj.children = fn(data[i].children);
        } else if (!data[i].folder) {
            obj.icon = "jstree-file";
        }
        arr.push(obj);
    }
    return arr;
}

function returnFun(fn) {
    return function() {
        fn();
    };
}

function getNodeFileDetail(node) {
    var promise = $.post("manage/file", {
        filePath: node.data.filePath,
        method: "get"
    });
    return promise;
}

function updateFile(filePath, newContent) {
    var promise = $.post("manage/file", {
        filePath: filePath,
        method: "update",
        content: newContent
    });
    return promise;
}

function createFile(filePath, newContent) {
    var promise = $.post("manage/file", {
        filePath: filePath,
        method: "create",
        content: newContent
    });
    return promise;
}
var banner = new PathBanner(document.getElementsByClassName("right-panel")[0]);
banner.showPath([{
    text: "Root"
}]);

function loadData(){
	var menu = new ContextMenu();
	$.post("manage/action").then(function(data) {
	    var roots = transToJsTreeJson([$.parseJSON(data)]);
	    roots[0].state = {
	        opened: true
	    };
	    var treeEl = $('#tree').jstree({
	        'core': {
	            'multiple': true,
	            'data': roots
	        }
	    });
	    var tree = $.jstree.reference(treeEl);
	    treeEl.on("after_open.jstree", returnFun(Context.adjustLayout));
	    treeEl.on("after_close.jstree", returnFun(Context.adjustLayout));
	    treeEl.on("click.jstree", returnFun(Context.adjustLayout));
	    treeEl.on("loaded.jstree", returnFun(Context.adjustLayout));
	    treeEl.on("dblclick.jstree", ".jstree-anchor", function(e) {
	        var node = tree.get_node(e.target);
	        if (node.data.isFolder) {
	            return;
	        }
	        getNodeFileDetail(node).then(function(nodeFile) {
	            nodeFile = $.parseJSON(nodeFile);
	            showFileDetail(nodeFile);
	            $("#update-btn").unbind("click");
	            $("#update-btn").on("click", function() {
	                updateFile(node.data.filePath, $(".text-content").val());
	            });
	        });
	    });
	    treeEl.on("contextmenu.jstree", ".jstree-anchor",function(e){
	    	e.preventDefault();
	    	var node = tree.get_node(e.target);
	    	var menuItems = [{
	    		text:"Create",
	    		onclick: function(){
	    			
	    		}
	    	},{
	    		text:"Delete",
	    		onclick: function(){
	    			MsgUtils.confirm("是否确定删除",function(){
	    				$.post("manage/file",{
		    				method: "delete",
		    				filePath: node.data.filePath
		    			}).then(function(res){
		    				if(res === 'Success'){
		    					
		    				}
		    			});
	    			});
	    		}
	    	}];
	    	if(!node.data.isFolder){
	    		menuItems.push({
		    		text:"Download",
		    		onclick: function(){
		    			var path = node.data.filePath;
		    			var form = $("<form></form>");
		    			var input1 = $("<input></input>"),
		    				input2 = $("<input></input>");
		    			input1.attr("name","method");
		    			input1.val("download");
		    			input2.attr("name","filePath");
		    			input2.val(path);
		    			form.append(input1);
		    			form.append(input2);
		    			form.attr("action","manage/file");
		    			form.attr("target","downloadFile");
		    			form.appendTo("body");
		    			form[0].submit();
		    			form.remove();
		    		}
		    	});
	    	}
	    	menu.setMenuItems(menuItems);
	    	tree.deselect_all();
	    	tree.select_node(node);
	    	menu.showAt({
	    		x: e.pageX,
	    		y: e.pageY
	    	});
	    });
	    treeEl.on("click.jstree", ".jstree-anchor", function(e) {
	        var node = tree.get_node(e.target),
	            pathTexts = tree.get_path(node),
	            paths = [];
	        for (var i = pathTexts.length - 1; i >= 0; i--) {
	            var obj = {
	                text: pathTexts[i],
	                path: pathTexts.slice(0, i),
	                node: node
	            }
	            node = tree.get_node(node.parent);
	            paths.unshift(obj);
	        }
	        var curNode = tree.get_node(e.target);

	        banner.showPath(paths, function(path, index) {
	            tree.deselect_all();
	            tree.select_node(path.node);
	            if (!path.node.data.isFolder) {
	                showNodeFile(path.node);
	            }
	            banner.showPath(paths.slice(0, index + 1), arguments.callee);
	        });
	    });
	});
}
loadData();
</script>

</html>
