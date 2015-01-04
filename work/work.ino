//work.ino
#define abs(a) (((a) > 0)?(a):(-a))
int out[5] = {-1,6,7,8,9};//对应L298N上的In1,2,3,4号引脚，控制OUT1,2,3,4供电
int Left = 10,Right = 11;//对应左右电机的使能信号，利用PWM控制转速
void LMove(int u) {//控制左边的电机
	switch (u) {
		case -1://反转
		digitalWrite(out[1], HIGH);
		digitalWrite(out[2], LOW);
		break;
		case 1://正转
		digitalWrite(out[1], LOW);
		digitalWrite(out[2], HIGH);
		break;
		default://停止
		digitalWrite(out[1], LOW);
		digitalWrite(out[2], LOW);
	}
}
void RMove(int u) {//控制右边的电机
	switch (u) {
		case -1://反转
		digitalWrite(out[4], HIGH);
		digitalWrite(out[3], LOW);
		break;
		case 1://正转
		digitalWrite(out[4], LOW);
		digitalWrite(out[3], HIGH);
		break;
		default://停止
		digitalWrite(out[4], LOW);
		digitalWrite(out[3], LOW);
	}
}
void setup() {
	Serial.begin(9600);//打开串口
	analogWrite(Left, 255);//默认使能最高
	analogWrite(Right, 255);
	for (int i = 1;i <= 4;i++) {
		pinMode(out[i], OUTPUT);
	}
}
void loop() {
	int v;
	while (Serial.available()) {
		char ch = Serial.read();
		Serial.write(ch);//向Android手机反馈信息，反馈的就是得到的字符
		switch(ch) {
			case 'w'://前进
					RMove(1);
					LMove(1);
				break;
			case 's'://后退
					RMove(-1);
					LMove(-1);
				break;
			case 'a'://左转
					LMove(0);
					RMove(1);
				break;
			case 'd'://右转
					LMove(1);
					RMove(0);
				break;
			case 'p'://停止
					LMove(0);
					RMove(0);
				break;
			case 'l'://改变左边电机的使能信号，但不改变电机的控制信号
			case 'L'://改变左边电机的使能信号，同时改变电机的控制信号
				v = Serial.parseInt();
				if (ch == 'L') {
					if (v > 80) LMove(1);
					else if (v < -80) LMove(-1);
					else LMove(0);
				}
				analogWrite(Left, abs(v));;
				break;
			case 'r'://改变右边电机的使能信号，但不改变电机的控制信号
			case 'R'://改变右边电机的使能信号，同时改变电机的控制信号
				v = Serial.parseInt();
				if (ch == 'R') {
					if (v > 80) RMove(1);
					else if (v  < -80) RMove(-1);
					else RMove(0);
				}
				analogWrite(Right, abs(v));
				break;
		}
	}
}
