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
    //{name,type,options}
    prompt: function(opt, cb) {
    	if(!opt.type){
    		opt.type = "text";
    	}
    	switch(opt.type){
    		case "text": {
    			
    		}
    	}
    	
    	function textInput(){
    		
    	}
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