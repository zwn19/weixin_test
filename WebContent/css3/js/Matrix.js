	function Matrix(){
		var arrs = arguments,
			matrix = [];
		if(arguments.length > 1){
			for(var i=0,len=arrs.length;i<len;i++){
				matrix.push(arrs[i]);
			}
		}else if(arguments[0].length > 1){
			matrix = arguments[0];
		}
		this.matrix = matrix;
	}
	Matrix.getRotateMatrix = function(x,y,z,angle){
		var len = Math.sqrt(x*x + y*y + z*z);
		x = x/len;
		y = y/len;
		z = z/len;
		var cos = Math.cos(angle/180*Math.PI);
		var sin = Math.sin(angle/180*Math.PI);
		var _cos = 1-cos;
		var arr1 = [x*x*_cos+cos,x*y*_cos+z*sin,x*z*_cos-y*sin];
		var arr2 = [x*y*_cos-z*sin,y*y*_cos+cos,y*z*_cos+x*sin];
		var arr3 = [y*z*_cos+y*sin,y*z*_cos-x*sin,z*z*_cos+cos];
		return new Matrix([arr1,arr2,arr3]);
	};
	Matrix.getTranslateMatrix2D = function(x,y){
		var arr = [[1,0,0],[0,1,0],[x,y,0]];
		return new Matrix(arr);
	};
	Matrix.getCssTransformMatrix2D = function(ele){
		var style = getComputedStyle(ele);
		var cur = style.transform.replace(/matrix/g,"");
		var eles = cur.split(",");
		var arr = [[eles[0],eles[1],0],[eles[2],eles[3],0],[eles[4],eles[5],1]];
		return new Matrix(arr);
	};
	Matrix.toCssTransform2D = function(matrix){
		var data = matrix.getData();
		var arr = [data[0][0],data[0][1],data[1][0],data[1][1],data[2][0],data[2][1]];
		return arr.join(",");
	};
	Matrix.toCssTransform3D = function(matrix){
		var data = matrix.getData();
		var arr = [data[0][0],data[0][1],data[0][2],0,data[1][0],data[1][1],data[1][2],0,data[2][0],data[2][1],data[2][2],0,0,0,0,1];
		return arr.join(",");
	};
	Matrix.prototype = {
		getData: function(){
			return this.matrix;
		},	
		isSameType: function(matrix){
			if(matrix instanceof Matrix){
				if(this.rowLength() === matrix.rowLength() && this.columnLength() === matrix.columnLength()){
					return true;
				}
			}
			return false;
		},
		rowLength: function(){
			return this.matrix.length;
		},
		columnLength: function(){
			return this.matrix[0].length;
		},
		//pos:{row,col}
		getElement: function(pos){
			return this.matrix[pos.row][pos.col];
		},
		setElement: function(pos,val){
			this.matrix[pos.row][pos.col] = val;
		},
		add: function(matrix,factor){
			if(this.isSameType(matrix)){
				factor = factor || 1;
				var rowLen = this.matrix.length,
					collen = this.matrix[0].length;
				for(var i=0;i<rowLen;i++){
					for(var j=0;j<collen;j++){
						var pos = {col:j,row:i};
						var val = matrix.getElement(pos) * factor + this.getElement(pos);
						this.setElement(pos, val);
					}
				}
				return this;
			}
			throw new Error("The input is not the same type matrix");
		},
		minus: function(matrix){
			return this.add(matrix, -1);
		},
		multiply: function(matrix){
			if(matrix instanceof Matrix){
				if(this.columnLength() === matrix.rowLength()){
					var reversed = matrix.copy().reverse();
					var rowLen = this.matrix.length;
					var ret = [];
					for(var i=0;i<rowLen;i++){
						ret[i] = [];
						for(var j=0;j<rowLen;j++){
							ret[i][j] = arrMulti(this.matrix[i],reversed.matrix[j]);
						}
					}
					this.matrix = ret;
				}
			}else{
				matrix = matrix * 1;
				if(!isNaN(matrix)){
					var rowLen = this.matrix.length,
						collen = this.matrix[0].length;
					for(var i=0;i<rowLen;i++){
						for(var j=0;j<collen;j++){
							this.matrix[i][j] = this.matrix[i][j] * matrix;
						}
					}
				}
			}
			return this;
			function arrMulti(arr1,arr2){
				var ret = 0;
				for(var i=0,len=arr1.length;i<len;i++){
					ret += arr1[i] * arr2[i];
				}
				return ret;
			}
			throw new Error("Can not multiply " + matrix);
		},
		determinant: function(){
			if(this.rowLength() === this.columnLength()){
				return determinant(this.matrix);
			}else{
				throw new Error("Can not get determinant,rowLength=" + this.rowLength() + " and columnLength=" + this.columnLength());
			}
			function determinant(arr2d){
				if(arr2d.length === 2){
					return arr2d[0][0] * arr2d[1][1] - arr2d[0][1] * arr2d[1][0];
				}else{
					var	collen = arr2d[0].length,
						sum = 0;
					for(var j=0;j<collen;j++){
						var pos = {col:j,row:0};
						var cofactor = getCofactor(arr2d,pos);
						sum += arr2d[0][j] * determinant(cofactor) * Math.pow(-1,j);
					}
					return sum;
				}
			}
			//pos:{row,col},求余子式
			function getCofactor(arr2d,pos){
				var ret = [];
				for(var i=0,len=arr2d.length;i<len;i++){
					if(i === pos.row){
						continue;
					}
					ret.push([]);
					for(var j=0,l=arr2d[i].length;j<l;j++){
						if(j===pos.col){
							continue;
						}
						ret[ret.length-1].push(arr2d[i][j]);
					}
				}
				return ret;
			}
		},
		reverse: function(){
			var ret = [];
			var rowLen = this.matrix.length,
				collen = this.matrix[0].length;
			for(var i=0;i<rowLen;i++){
				for(var j=0;j<collen;j++){
					ret[j] = ret[j] || [];
					ret[j][i] = this.matrix[i][j];
				}
			}
			this.matrix = ret;
			return this;
		},
		copy: function(){
			var ret = [];
			var rowLen = this.matrix.length,
				collen = this.matrix[0].length;
			for(var i=0;i<rowLen;i++){
				ret[i] = [];
				for(var j=0;j<collen;j++){
					ret[i].push(this.matrix[i][j]);
				}
			}
			return new Matrix(ret);
		},
		toString: function(){
			var strArr = [];
			var rowLen = this.matrix.length;
			for(var i=0;i<rowLen;i++){
				strArr.push(this.matrix[i].join(" , "));
			}
			return strArr.join("\n");
		}
	}