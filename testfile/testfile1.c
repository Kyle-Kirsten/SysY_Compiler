#include <stdio.h>
/*编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef  // 1.是否存在Decl 2.是否存在FuncDef

    声明 Decl → ConstDecl | VarDecl // 覆盖两种声明

    常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次

    基本类型 BType → 'int' // 存在即可

    常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal  // 包含普通变量、一维数组、二维数组共三种情况

    常量初值 ConstInitVal → ConstExp
        | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值

    变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复多次

    变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
        | Ident { '[' ConstExp ']' } '=' InitVal

    变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值

    函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参

    主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数*/
    
// 先整体覆盖一下 ，再考虑细节覆盖 
const int const1 = -2;
const int const4[3] = {1, -2, 3};
const int const5[3][-+-3] = {{1, 2, 3}, {4, 5, 6}, {7, 8 , 9}};
const int const2 = +-+4, const3 = 5;


int var1;
int var2 = 2;
int var3[2];
int var4[2] = {1, 2};
int var5[2][2];
int var6[2][2] = {{1, -1}, {-1, 1}};
int var7, var8;

/* 测试用函数，交作业时注释掉 */ 
int getint() { 
	int n;
 	scanf("%d",&n);
 	return n; 
} /* */

int func1() {
	return 0;
}

void func2() {
	return;
}

int func3(int a) {
	return a;
}

int func4(int a, int b[], int c[][2]) {
	return a + b[0] + c[0][0];
}

int func5() {
	int n = 0, var9, var10[2], var11[2][2];
	
	const int var12 = -+-114514;
	printf("-+-114514 = %d\n", var12);
	
	if (var12 > 0) {
		printf("-+-114514 > 0\n");
	}
	
	var9 = -+- 6 + 7 * (6 -9) / 3 / 2;
	printf("-+- 6 + 7 * (6 -9) / 3 / 2 = %d\n", var9);
	
	if (var9 > 0) {
		var10[1] = var9;
	} else {
		var10[1] = -0;
	}
	printf("var10 = %d\n", var10);
	
	int var13;
	if (var9 > 0) {
		var13 = getint();
	} else {
		var13 = 0;
	}
	printf("var13 = %d\n", var13);
	
	int var15;
	func2();
	
	var15 = func1();
	printf("func1() = %d\n", var15);
	
	var15 = func3(+-12);
	printf("func3(+-12) = %d\n", var15);
	
	var15 = func4(1, var4, var6);
	printf("func4(1, var4, var6) = %d\n", var15);
	
	var15 = func4(const1, var6[1], var6);
	printf("func4(const1, var6[1], var6) = %d\n", var15);
	
	
	{
		int var14 = var13;
		if (var14 > 0) {
			var14 = getint();
		} else {
			var14 = 0;
		}
		printf("var14 = %d\n", var14);
	}
	
	while (n < const3) {
		if (n > 3) {
			printf("n = %d > 3\n", n);
			break;
		} else{
			
		}
		n = n + 1;
		continue;
	}
	
	return 0;
}

int testExp() {
	int a = 6;
	
	/* 基本表达式 */
	a = 
	printf("")
	 
}

int main() {
	printf("19373163\n");
	func5();
	testExp();
	return 0;
}


