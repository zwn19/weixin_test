	function Vector2D(x,y){
		y = y || 0;
		this.init(x,y);
	}
	Vector2D.prototype = {
		init: function(x,y){
			var length = Math.sqrt(x*x + y*y);
			this.x = x/length;
			this.y = y/length;
			this.length = length;
		},
		getLength: function(){
			return this.length;
		},
		setX: function(x){
			var y = this.getY();
			this.init(x,y);
		},
		setY: function(y){
			var x = this.getX();
			this.init(x,y);
		},
		getX: function(){
			return this.x * this.length;
		},
		getY: function(){
			return this.y * this.length;
		},
		getVertical: function(){
			return new Vector(this.y*this.length,-this.x*this.length);
		},
		_getRotateMatrix: function(angle){
			var angle = angle/Math.PI*180;
			var _cos = Math.cos(angle),
				_sin = Math.sin(angle);
			var arr = [[_cos,_sin],[-_sin,_cos]];
			return new Matrix(arr);
		},
		_fnWarpper: function(x,y,fn){
			if(x instanceof Vector2D){
				y = x.getY();
				x = x.getX();
			}
			return fn.call(this,x,y);
		},
		_angle: function(x,y){
			var _x = this.getX(),
				_y = this.getY();
			var angle = Math.acos(this._multiply(x, y) / (this.getLength() + Math.sqrt(x*x + y*y)));
			return angle/Math.PI*180;
		},
		_minus: function(x,y){
			var _x = this.getX(),
				_y = this.getY();
			return new Vector2D(_x-x,_y-y);
		},
		_add: function(x,y){
			var _x = this.getX(),
				_y = this.getY();
			return new Vector2D(_x-x,_y-y);
		},
		_multiply: function(x,y){
			var _x = this.getX(),
				_y = this.getY();
			return _x*x + _y*y;
		},
		angle: function(x,y){
			return this._fnWarpper(x,y,this._angle);
		},
		minus: function(x,y){
			return this._fnWarpper(x,y,this._minus);
		},
		add: function(x,y){
			return this._fnWarpper(x,y,this._add);
		},
		multiply: function(x,y){
			return this._fnWarpper(x,y,this._multiply);
		},
		rotate: function(angle){
			var rm = this._getRotateMatrix(angle);
			var m = new Matrix([[this.getX(),this.getY()]]);
			rm.multiply(m);
			var data = rm.getData();
			this.setX(data[0][0]);
			this.setY(data[0][1]);
		}
	};