#pragma once
#include "memory.h"

struct aim_result_t {
    bool      found         = false;
    vec2_t    screen        = {};
    vec2_t    delta         = {};
    uintptr_t target_entity = 0;
};

void         aim_set_screen(float w, float h);
void         aim_set_smooth(float s);
void         aim_set_fov(float f);
bool         world_to_screen(const float* m, vec3_t w, vec2_t& s);
int          scan_entities(uintptr_t base, entity_data_t* out, int max, int32_t local_team);
aim_result_t aim_find_target(const entity_data_t* ents, int cnt, const float* m);
