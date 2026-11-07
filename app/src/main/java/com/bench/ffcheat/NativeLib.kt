package com.bench.ffcheat

object NativeLib {

    init {
        System.loadLibrary("ffcheat")
    }

    @JvmStatic external fun init(pid: Int = 0): Boolean
    @JvmStatic external fun setScreen(w: Float, h: Float)
    @JvmStatic external fun setAimSmooth(s: Float)
    @JvmStatic external fun setAimFov(f: Float)

    /**
     * Retorna FloatArray:
     * índices 0..(count*5-1) = entidades ESP [screenX, screenY, hp, maxHp, teamId]
     * índices count*5+0,+1   = aim delta X, Y
     * índice  count*5+2      = aim found (1.0 ou 0.0)
     * índice  count*5+3      = entity count
     */
    @JvmStatic external fun tick(): FloatArray?

    @JvmStatic external fun shutdown()
}
