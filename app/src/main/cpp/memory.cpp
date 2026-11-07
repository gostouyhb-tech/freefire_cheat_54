#include "memory.h"
#include <cstdio>
#include <cstring>
#include <fcntl.h>
#include <unistd.h>
#include <cstdlib>
#include <sys/uio.h>
#include <dirent.h>

static int      g_fd  = -1;
static pid_t    g_pid = 0;

pid_t find_process(const char* pkg) {
    DIR* d = opendir("/proc");
    if (!d) return 0;
    dirent* e;
    char path[64], buf[256];
    while ((e = readdir(d))) {
        for (char* c = e->d_name; *c; ++c)
            if (*c < '0' || *c > '9') goto next;
        snprintf(path, sizeof(path), "/proc/%s/cmdline", e->d_name);
        { int f = open(path, O_RDONLY);
          if (f < 0) goto next;
          ssize_t n = read(f, buf, sizeof(buf)-1); close(f);
          if (n > 0) { buf[n] = 0; if (strstr(buf, pkg)) { closedir(d); return atoi(e->d_name); } }
        }
        next:;
    }
    closedir(d);
    return 0;
}

bool mem_open(pid_t pid) {
    char path[64];
    snprintf(path, sizeof(path), "/proc/%d/mem", pid);
    g_fd  = open(path, O_RDONLY);
    g_pid = pid;
    return g_fd >= 0;
}

void mem_close() { if (g_fd >= 0) { close(g_fd); g_fd = -1; } }

bool mem_read(uintptr_t addr, void* buf, size_t sz) {
    iovec l = {buf, sz}, r = {(void*)addr, sz};
    ssize_t n = process_vm_readv(g_pid, &l, 1, &r, 1, 0);
    if (n != (ssize_t)sz && g_fd >= 0)
        n = pread64(g_fd, buf, sz, (off64_t)addr);
    return n == (ssize_t)sz;
}

float     mem_rf(uintptr_t a)  { float    v=0; mem_read(a,&v,4); return v; }
int32_t   mem_ri(uintptr_t a)  { int32_t  v=0; mem_read(a,&v,4); return v; }
uint64_t  mem_ru(uintptr_t a)  { uint64_t v=0; mem_read(a,&v,8); return v; }

uintptr_t get_module_base(pid_t pid, const char* mod) {
    char path[64]; snprintf(path, sizeof(path), "/proc/%d/maps", pid);
    FILE* f = fopen(path, "r"); if (!f) return 0;
    char line[512]; uintptr_t base = 0;
    while (fgets(line, sizeof(line), f))
        if (strstr(line, mod) && strstr(line, "r-xp"))
            { base = strtoull(line, nullptr, 16); break; }
    fclose(f);
    return base;
}
