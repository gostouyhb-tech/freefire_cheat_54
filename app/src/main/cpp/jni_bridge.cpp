#include <jni.h>
#include "memory.h"
#include "aimbot.h"
#include <cstring>

static uintptr_t  g_base = 0;
static entity_data_t g_ents[64];
static int        g_cnt  = 0;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bench_ffcheat_NativeLib_init(JNIEnv*, jclass, jint pid) {
    pid_t p = (pid_t)pid;
    if (!p) {
        p = find_process("com.dts.freefiremax");
        if (!p) p = find_process("com.dts.freefireth");
    }
    if (!p || !mem_open(p)) return JNI_FALSE;
    g_base = get_module_base(p, "libil2cpp.so");
    return g_base ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_bench_ffcheat_NativeLib_setScreen(JNIEnv*, jclass, jfloat w, jfloat h) { aim_set_screen(w,h); }

extern "C" JNIEXPORT void JNICALL
Java_com_bench_ffcheat_NativeLib_setAimSmooth(JNIEnv*, jclass, jfloat s) { aim_set_smooth(s); }

extern "C" JNIEXPORT void JNICALL
Java_com_bench_ffcheat_NativeLib_setAimFov(JNIEnv*, jclass, jfloat f) { aim_set_fov(f); }

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_bench_ffcheat_NativeLib_tick(JNIEnv* env, jclass) {
    uintptr_t lp = mem_ru(g_base + FF::OFF_LOCAL_PLAYER);
    int32_t lt   = lp ? mem_ri(lp + FF::OFF_LOCAL_TEAM) : -1;

    g_cnt = scan_entities(g_base, g_ents, 64, lt);

    float vp[16] = {};
    uintptr_t cm = mem_ru(g_base + FF::OFF_CAMERA_MANAGER);
    if (cm) mem_read(cm + FF::OFF_CAMERA_MATRIX, vp, sizeof(vp));

    aim_result_t aim = aim_find_target(g_ents, g_cnt, vp);

    jfloatArray arr = env->NewFloatArray(g_cnt*5 + 4);
    if (!arr) return nullptr;
    jfloat* b = env->GetFloatArrayElements(arr, nullptr);
    int idx = 0;

    for (int i = 0; i < g_cnt; ++i) {
        const entity_data_t& e = g_ents[i];
        vec2_t s{}; world_to_screen(vp, e.head_pos, s);
        b[idx++] = s.x;
        b[idx++] = s.y;
        b[idx++] = e.health;
        b[idx++] = e.max_health;
        b[idx++] = (float)e.team_id;
    }
    b[idx++] = aim.found ? aim.delta.x : 0.f;
    b[idx++] = aim.found ? aim.delta.y : 0.f;
    b[idx++] = aim.found ? 1.f : 0.f;
    b[idx]   = (float)g_cnt;

    env->ReleaseFloatArrayElements(arr, b, 0);
    return arr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_bench_ffcheat_NativeLib_shutdown(JNIEnv*, jclass) { mem_close(); }
