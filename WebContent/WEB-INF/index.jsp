<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="resources/css/bootstrap.css" />
    <link rel="stylesheet" href="resources/jstree/themes/default/style.css" />
    <link rel="stylesheet" href="css/manage.css" />
    <script src="resources/js/jquery.js"></script>
    <script src="resources/js/bootstrap.js"></script>
    <script src="resources/jstree/jstree.js"></script>
    <script src="js/manage.js"></script>
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
	            'data': roots,
	            'check_callback': true//let delete_node work
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
	    			MsgUtils.confirm('是否确定删除 "' + node.text +'"',function(){
	    				$.post("manage/file",{
		    				method: "delete",
		    				filePath: node.data.filePath
		    			}).then(function(res){
		    				tree.delete_node(node);
		    				if(res === 'Success'){
		    					tree.delete_node(node);
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
	    	}else{
	    		menuItems.push({
		    		text:"Refresh",
		    		onclick: function(){
		    			$.post("manage/file",{
		    				method: "refresh",
		    				filePath: node.data.filePath
		    			}).then(function(res){
		    				
		    			});
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
