package com.bench.ffcheat;

interface IMemoryService {
    boolean init();
    float[] tick();
    void shutdown();
    void setConfig(float smooth, float fov, float screenW, float screenH);
}
