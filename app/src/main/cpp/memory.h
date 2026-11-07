#pragma once
#include <cstdint>
#include <cstddef>
#include <sys/types.h>
#include <cerrno>
#include <cstring>
#include <sys/uio.h>

namespace FF {
    constexpr uintptr_t OFF_ENTITY_MANAGER  = 0x065A3B10;
    constexpr uintptr_t OFF_ENTITY_LIST     = 0x18;
    constexpr uintptr_t OFF_LIST_ITEMS      = 0x10;
    constexpr uintptr_t OFF_LIST_SIZE       = 0x18;
    constexpr uintptr_t OFF_ENTITY_HEALTH   = 0xEC;
    constexpr uintptr_t OFF_ENTITY_MAXHP    = 0xF0;
    constexpr uintptr_t OFF_ENTITY_TEAM     = 0x128;
    constexpr uintptr_t OFF_ENTITY_ALIVE    = 0x154;
    constexpr uintptr_t OFF_ENTITY_POSITION = 0x1A4;
    constexpr uintptr_t OFF_ENTITY_BONES    = 0x230;
    constexpr uintptr_t OFF_BONE_ARRAY_DATA = 0x20;
    constexpr int       BONE_HEAD           = 11;
    constexpr uintptr_t OFF_CAMERA_MANAGER  = 0x065A4020;
    constexpr uintptr_t OFF_CAMERA_MATRIX   = 0xDC;
    constexpr uintptr_t OFF_LOCAL_PLAYER    = 0x065A3C80;
    constexpr uintptr_t OFF_LOCAL_TEAM      = 0x128;
}

struct vec3_t { float x, y, z; };
struct vec2_t { float x, y; };

struct entity_data_t {
    uintptr_t ptr;
    vec3_t    world_pos;
    vec3_t    head_pos;
    float     health;
    float     max_health;
    int32_t   team_id;
    bool      is_alive;
    bool      is_valid;
};

pid_t     find_process(const char* pkg);
bool      mem_open(pid_t pid);
void      mem_close();
bool      mem_read(uintptr_t addr, void* buf, size_t sz);
float     mem_rf(uintptr_t a);
int32_t   mem_ri(uintptr_t a);
uint64_t  mem_ru(uintptr_t a);
uintptr_t get_module_base(pid_t pid, const char* mod);
