#include "aimbot.h"
#include "memory.h"
#include <cmath>

static float g_sw = 1920.f, g_sh = 1080.f;
static float g_smooth = 0.25f;
static float g_fov    = 350.f;

void aim_set_screen(float w, float h) { g_sw = w; g_sh = h; }
void aim_set_smooth(float s)          { g_smooth = s; }
void aim_set_fov(float f)             { g_fov = f; }

bool world_to_screen(const float* m, vec3_t w, vec2_t& s) {
    float cx = w.x*m[0]  + w.y*m[4]  + w.z*m[8]  + m[12];
    float cy = w.x*m[1]  + w.y*m[5]  + w.z*m[9]  + m[13];
    float cw = w.x*m[3]  + w.y*m[7]  + w.z*m[11] + m[15];
    if (cw < 0.1f) return false;
    s.x = (cx/cw + 1.f) * .5f * g_sw;
    s.y = (1.f - cy/cw) * .5f * g_sh;
    return true;
}

int scan_entities(uintptr_t base, entity_data_t* out, int max, int32_t local_team) {
    uintptr_t em    = mem_ru(base + FF::OFF_ENTITY_MANAGER); if (!em) return 0;
    uintptr_t list  = mem_ru(em   + FF::OFF_ENTITY_LIST);    if (!list) return 0;
    uintptr_t items = mem_ru(list + FF::OFF_LIST_ITEMS);
    int32_t   cnt   = mem_ri(list + FF::OFF_LIST_SIZE);
    if (!items || cnt <= 0 || cnt > 128) return 0;

    int found = 0;
    for (int i = 0; i < cnt && found < max; ++i) {
        uintptr_t e = mem_ru(items + 0x20 + i*8); if (!e) continue;
        if (!mem_ri(e + FF::OFF_ENTITY_ALIVE))     continue;
        if (mem_ri(e + FF::OFF_ENTITY_TEAM) == local_team) continue;
        float hp = mem_rf(e + FF::OFF_ENTITY_HEALTH); if (hp <= 0.f) continue;

        entity_data_t& d = out[found];
        d.ptr      = e;
        d.health   = hp;
        d.max_health = mem_rf(e + FF::OFF_ENTITY_MAXHP);
        d.team_id  = mem_ri(e + FF::OFF_ENTITY_TEAM);
        d.is_alive = true;
        d.is_valid = true;
        mem_read(e + FF::OFF_ENTITY_POSITION, &d.world_pos, sizeof(vec3_t));

        uintptr_t ba = mem_ru(e + FF::OFF_ENTITY_BONES);
        if (ba) {
            uintptr_t bd = mem_ru(ba + FF::OFF_BONE_ARRAY_DATA);
            if (bd) mem_read(bd + FF::BONE_HEAD*12, &d.head_pos, sizeof(vec3_t));
        }
        if (!d.head_pos.x && !d.head_pos.y)
            d.head_pos = { d.world_pos.x, d.world_pos.y + 1.75f, d.world_pos.z };

        ++found;
    }
    return found;
}

aim_result_t aim_find_target(const entity_data_t* ents, int cnt, const float* m) {
    aim_result_t r{};
    float best = g_fov * g_fov;
    float cx = g_sw*.5f, cy = g_sh*.5f;

    for (int i = 0; i < cnt; ++i) {
        const entity_data_t& e = ents[i];
        if (!e.is_valid || !e.is_alive) continue;
        vec2_t s{};
        if (!world_to_screen(m, e.head_pos, s)) continue;
        if (s.x < 0 || s.x > g_sw || s.y < 0 || s.y > g_sh) continue;
        float dx = s.x-cx, dy = s.y-cy;
        float d2 = dx*dx + dy*dy;
        if (d2 < best) {
            best = d2;
            r.found  = true;
            r.screen = s;
            r.delta  = { dx*g_smooth, dy*g_smooth };
            r.target_entity = e.ptr;
        }
    }
    return r;
}
