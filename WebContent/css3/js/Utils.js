(function(){
	var Utils = window.Utils || {};
	function addClass(ele,className){
		var classes = ele.className.split(" ");
		if(classes.indexOf(className) === -1){
			classes.push(className);
			ele.className = classes.join(" ");
		}
	}
	function removeClass(ele,className){
		var classes = ele.className.split(" ");
		if(classes.indexOf(className) > -1){
			classes = classes.filter(function(ele){return ele !== className});
			ele.className = classes.join(" ");
		}
	}
	
	function compareObject(org,tar,deep){
		var ret;
		if(typeof(org) === 'object' && typeof(tar) === 'object'){
			compare(org,tar,"");
			return ret;
		}else{
			throw new Error("at least one parameter not Object");
		}
		
		function compare(_org,_tar,curPath){
			var compared = [];
			for(var p in _org){
				compared.push(p);
				var _cur = curPath ? curPath+"."+p : p;
				if(typeof(_org[p]) !== typeof(_tar[p])){
					ret = ret || {};
					ret[_cur] = [_org[p],_tar[p]];
				}
				else if(typeof(_org[p]) === 'object'){
					compare(_org[p],_tar[p],_cur);
				}
				else if(_org[p] !== _tar[p]){
					ret = ret || {};
					ret[_cur] = [_org[p],_tar[p]];
				}
			}
			for(var p in _tar){
				if(compared.indexOf(p) > -1){
					continue;
				}
				var _cur = curPath ? curPath+"."+p : p;
				ret = ret || {};
				ret[_cur] = [undefined,_tar[p]];
			}
		}
	}
	
	function copyObject(origin,deep){
		if(isObject(origin) || isArray(origin)){
			deep = deep || 100;
			return copy(origin,0);
		}else{
			return origin;
		}
		function copy(obj,curDep){
			if(curDep > deep){
				throw new Error("Current deep is more than " + deep);
			}
			var ret;
			if(isArray(obj)){
				ret = [];
				for(var i=0,len=obj.length;i<len;i++){
					if(isFunction(obj[i])){
						continue;
					}
					if(isObject(obj[p]) || isArray(origin)){
						ret[i] = copy(obj[i],curDep+1);
					}
					else{
						ret[i] = obj[i];
					}
				}
			}else{
				ret = {};
				for(var p in obj){
					if(isFunction(obj[p])){
						continue;
					}
					if(isObject(obj[p]) || isArray(origin)){
						ret[p] = copy(obj[p],curDep+1);
					}
					else{
						ret[p] = obj[p];
					}
				}
			}
			return ret;
		}
	}
	
	function searchProperty(obj,condition,deep){
		deep = deep || 10;
		var match,ret;
		if(isString(condition)){
			match = function(p){
				return p === condition;
			};
		}else if(isRegExp(condition)){
			match = function(p){
				return condition.test(p);
			};
		}else if(isFunction(condition)){
			match = function(p,val,path,dept){
				return condition(p,val,path,dept);
			};
		}
		else{
			throw new Error("Condition is neither string, regexp nor function");
		}
		search(obj,"",1);
		function search(_obj,curPath,curDept){
			if(curDept > deep){
				return;
			}
			for(var p in _obj){
				var _cur = curPath ? curPath+"."+p : p;
				if(typeof(_obj[p]) === 'object'){
					search(_obj[p],_cur,curDept + 1)
				}
				else if(match(p,_obj[p],_cur,curDept)){
					ret = ret || [];
					ret.push(_cur);
				}
			}
		}
		return ret;
	}
	var Queue = {
		array: [],
		_current: null,
		_run: function(){
			if(this.array.length && !this._current){
				var cur = array.pop();
				cur.fn();
			}
		},
		push: function(fn,interval){
			var me = this;
			this.array.push({
				fn: function(){
					me._current = setTimeout(function(){
						if(fn()){
							clearTimeout(me._current);
							me._run();
						}
					},interval);
				}
			});
			me._run();
		}
	};
	
	function pushTask(fn,interval){
		interval = interval || 100;
		Queue.push(fn, interval);
	}
	
	toString = Object.prototype.toString;
	
	function isNumber(input){
		return toString.call(input) === "[object Number]";
	}
	function isFunction(input){
		return toString.call(input) === "[object Function]";
	}
	function isBoolean(input){
		return toString.call(input) === "[object Boolean]";
	}
	function isString(input){
		return toString.call(input) === "[object String]";
	}
	function isObject(input){
		return toString.call(input) === "[object Object]";
	}
	function isArray(input){
		return toString.call(input) === "[object Array]";
	}
	function isDate(input){
		return toString.call(input) === "[object Date]";
	}
	function isRegExp(input){
		return toString.call(input) === "[object RegExp]";
	}
	Utils.isNumber = isNumber;
	Utils.isFunction = isFunction;
	Utils.isBoolean = isBoolean;
	Utils.isString = isString;
	Utils.isArray = isArray;
	Utils.isDate = isDate;
	Utils.isRegExp = isRegExp;
	Utils.copyObject = copyObject;
	Utils.compareObject = compareObject;
	Utils.addClass = addClass;
	Utils.removeClass = removeClass;
	Utils.searchProperty = searchProperty;
	Utils.pushTask = pushTask;
	window.Utils = Utils;
})();	