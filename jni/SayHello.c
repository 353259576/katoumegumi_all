#include "com_ws_java_experiment_listener_GccTest.h"

JNIEXPORT void JNICALL Java_com_ws_java_experiment_listener_GccTest_sayHello(JNIEnv * e, jobject jo) {
	printf("Hello World!\n");
	return;
}