#include <jni.h>
#include <string>
#define JCHAR_MAX 65535
#define CHAR_WIDTH_MIN 10
#define JCHAR_SPACE 32
#define JCHAR_TAB 9
#define TAB_SIZE_DEFAULT 4
#define TMP_SIZE 512

static jmethodID _measureText = NULL, _drawText = NULL;

struct CW {
	float* CHAR_WIDTHS;
	int TABSize;
	jchar* TMP;
	CW() {
		this->CHAR_WIDTHS = new float[JCHAR_MAX];
		this->TABSize = TAB_SIZE_DEFAULT;
	}
};

namespace CW_jni {
	static jlong createCW(JNIEnv *env, jclass clz) {
		CW *cw = new CW();
		cw->TMP = new jchar[TMP_SIZE];
		return reinterpret_cast<jlong>(cw);
	}

	static void loadCW(JNIEnv *env, jclass clz, jlong pointer, jobject paint) {
		CW *cw = reinterpret_cast<CW*>(pointer);
		jchar c = JCHAR_SPACE;
		jchar* p = &c;
		jcharArray arr = env->NewCharArray(1);
		env->SetCharArrayRegion(arr, 0, 1, p);
		cw->CHAR_WIDTHS[JCHAR_SPACE] = env->CallFloatMethod(paint, _measureText, arr, 0, 1);
		cw->CHAR_WIDTHS[JCHAR_TAB] = cw->CHAR_WIDTHS[JCHAR_SPACE] * cw->TABSize;
		for (c=0;c<JCHAR_MAX;c++) {
			if (c==JCHAR_SPACE||c==JCHAR_TAB) continue;
			env->SetCharArrayRegion(arr, 0, 1, p);
			if ((cw->CHAR_WIDTHS[c] = env->CallFloatMethod(paint, _measureText, arr, 0, 1)) < CHAR_WIDTH_MIN)
				cw->CHAR_WIDTHS[c] = CHAR_WIDTH_MIN;
		}
	}

	static jfloat getCW(JNIEnv *env, jclass clz, jlong pointer, jchar c) {
		return (reinterpret_cast<CW*>(pointer))->CHAR_WIDTHS[c];
	}

	static void setTABSize(JNIEnv *env, jclass clz, jlong pointer, jint size) {
		CW *cw = reinterpret_cast<CW*>(pointer);
		cw->TABSize = size;
		cw->CHAR_WIDTHS[JCHAR_TAB] = cw->CHAR_WIDTHS[JCHAR_SPACE] * size;
	}

	static jint getTABSize(JNIEnv *env, jclass clz, jlong pointer) {
		return (reinterpret_cast<CW*>(pointer))->TABSize;
	}

	static jfloat getDrawChars(JNIEnv *env, jclass clz, jlong pointer, jfloat xst, jfloat y, jfloat xleft, jfloat xright, jcharArray cs, jint st, jint en, jint cpst, jint cpen, jint cursor, jobject paint, jobject canvas, jcharArray ret, jfloatArray ret2) {
		CW *cw = reinterpret_cast<CW*>(pointer);
		jchar* S = new jchar[en-st];
		env->GetCharArrayRegion(cs, st, en-st, S);
		jcharArray ARR = env->NewCharArray(en-st);
		int tot = 0;
		en-=st;
		cpst-=st;
		cpen-=st;
		cursor-=st;
		st = 0;
		float wtmp;
		float x = xst;
		if (xleft > x)
			while ((wtmp = x + cw->CHAR_WIDTHS[cw->TMP[st]]) < xleft) {
				if (++st >= en) return -1;
				x = wtmp;
			}
		for (; st < en && x <= xright; st++) {
			if (st == cpst)
				env->SetFloatArrayRegion(ret2, 0, 1, &x);
			if (st == cursor)
				env->SetFloatArrayRegion(ret2, 2, 1, &x);
			if ((cw->TMP[tot] = S[st]) == JCHAR_TAB) {
				if (tot) {
					env->SetCharArrayRegion(ARR, 0, tot, cw->TMP);
					env->CallVoidMethod(canvas, _drawText, ARR, 0, tot, xst, y, paint);
					tot = 0;
					xst = x;
				}
				x += cw->CHAR_WIDTHS[JCHAR_TAB];
			} else
				x += cw->CHAR_WIDTHS[cw->TMP[tot++]];
			if (st == cpen - 1 && cpst != -1)
				env->SetFloatArrayRegion(ret2, 1, 1, &x);
		}
		if (st == cursor)
			env->SetFloatArrayRegion(ret2, 2, 1, &x);
		if (tot) {
        	env->SetCharArrayRegion(ARR, 0, tot, cw->TMP);
        	env->CallVoidMethod(canvas, _drawText, ARR, 0, tot, xst, y, paint);
        }
        delete[] S;
//        delete ARR;
        return x;
	}
}

static JNINativeMethod NATIVE_METHODS[] = {
	{"nCreateCW", "()J", (void*) CW_jni::createCW},
	{"nLoadCW", "(JLandroid/graphics/Paint;)V", (void*) CW_jni::loadCW},
	{"nGetCW", "(JC)F", (void*) CW_jni::getCW},
	{"nSetTABSize", "(JI)V", (void*) CW_jni::setTABSize},
	{"nGetTABSize", "(J)I", (void*) CW_jni::getTABSize},
	{"nGetDrawChars", "(JFFFF[CIIIIILandroid/graphics/Paint;Landroid/graphics/Canvas;[C[F)F", (void*) CW_jni::getDrawChars}
};

static bool RegisterNativeMethods(JNIEnv *env) {
	jclass clz = env->FindClass("com/xsjiong/vedit/VEdit");
	if (clz == NULL) return false;
	if (env->RegisterNatives(clz, NATIVE_METHODS, sizeof(NATIVE_METHODS) / sizeof(JNINativeMethod)) < 0)
		return false;
	return true;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
	JNIEnv *env = NULL;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK)
		return -1;
	if (!RegisterNativeMethods(env))
		return -1;
	jclass clz = env->FindClass("android/graphics/Paint");
	_measureText = env->GetMethodID(clz, "measureText", "([CII)F");
	clz = env->FindClass("android/graphics/Canvas");
	_drawText = env->GetMethodID(clz, "drawText", "([CIIFFLandroid/graphics/Paint;)V");
	return JNI_VERSION_1_6;
}