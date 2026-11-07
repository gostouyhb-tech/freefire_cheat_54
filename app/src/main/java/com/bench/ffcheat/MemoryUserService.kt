package com.bench.ffcheat

import android.os.IBinder

class MemoryUserService : IMemoryService.Stub() {

    override fun init(): Boolean {
        return NativeLib.init(0)
    }

    override fun tick(): FloatArray? {
        return NativeLib.tick()
    }

    override fun shutdown() {
        NativeLib.shutdown()
    }

    override fun setConfig(smooth: Float, fov: Float, screenW: Float, screenH: Float) {
        NativeLib.setScreen(screenW, screenH)
        NativeLib.setAimSmooth(smooth)
        NativeLib.setAimFov(fov)
    }

    class Binder : IBinder by MemoryUserService().asBinder()
}
