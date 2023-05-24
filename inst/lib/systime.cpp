#include "edu_iscas_tcse_favtrigger_instrumenter_SysTime.h"
#include <unistd.h>
#include <sys/time.h>
#include <time.h>

#define ONE_BILLION  1000000000L

JNIEXPORT jlong JNICALL Java_edu_iscas_tcse_favtrigger_instrumenter_SysTime_rdtsc (JNIEnv *, jclass)
{
	unsigned int lo, hi;
	asm volatile (
		"rdtsc"
		: "=a"(lo), "=d"(hi) /* outputs */
		: "a"(0)             /* inputs */
		: "%ebx", "%ecx");     /* clobbers*/
	long long x = ((unsigned long long)lo) | (((unsigned long long)hi) << 32);
	return (jlong)x;
}

JNIEXPORT jlong JNICALL Java_edu_iscas_tcse_favtrigger_instrumenter_SysTime_rdtscp (JNIEnv *, jclass)
{
	unsigned int lo, hi;
	asm volatile (
		"rdtscp"
		: "=a"(lo), "=d"(hi) /* outputs */
		: "a"(0)             /* inputs */
		: "%ebx", "%ecx");     /* clobbers*/
	long long x = ((unsigned long long)lo) | (((unsigned long long)hi) << 32);
	return (jlong)x;
}
