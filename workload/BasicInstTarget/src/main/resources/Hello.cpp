#include <iostream>
#include "fwh_NativeMain.h"
 
using namespace std;
 
JNIEXPORT void JNICALL Java_fwh_NativeMain_Hello
  (JNIEnv *, jclass)
{
    cout << "Hello,fwh!" << endl;
}
