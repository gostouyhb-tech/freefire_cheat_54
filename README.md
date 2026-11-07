# Free Fire Cheat — Android 15 / AArch64

## Requisitos
- Android 15 (API 35), dispositivo AArch64
- Root ativo (Magisk 27+ / KernelSU 1.0+)
- Android Studio Ladybug (2024.2.1) ou mais novo
- NDK r27+, CMake 3.22.1+
- Free Fire MAX ou Free Fire Global instalado

---

## Setup de Build

```bash
# 1. Clone / abra o projeto no Android Studio
# 2. Sync gradle
# 3. Build > Generate Signed APK (ou debug para teste)
```

Instale via adb com `-r` (reinstall):
```bash
adb install -r app-release.apk
```

---

## Pré-requisitos no dispositivo

### Magisk
1. Enable Zygisk
2. DenyList: adicione Free Fire ao DenyList  
   → Menu Magisk → Configurações → DenyList → ative Free Fire  
   → Isso esconde o root do anticheat do jogo

### Permissões
Após instalar, abra o app UMA vez:
1. Aceite "Exibir sobre outros apps"
2. Vá em Configurações → Acessibilidade → Game Assistant → Ativar

---

## Arquitetura

```
MainActivity
  └── solicita SYSTEM_ALERT_WINDOW
  └── inicia CheatService (foreground)

CheatService (Coroutine loop ~60fps)
  ├── NativeLib.init()     → encontra PID do FF, abre /proc/<pid>/mem
  ├── NativeLib.tick()     → scan entidades, calcula ESP + aimbot delta
  ├── OverlayView.update() → redesenha ESP via Canvas
  └── AimAccessibilityService.injectAimMove(dx, dy)

AimAccessibilityService
  └── dispatchGesture() → swipe suave para mover a mira

libffcheat.so (NDK C++17)
  ├── memory.cpp   → process_vm_readv / /proc/mem fd
  ├── aimbot.cpp   → world_to_screen, scan_entities, aim_find_target
  └── jni_bridge.cpp → JNI exports
```

---

## Configuração de Jogo (config no código)

| Parâmetro | Padrão | Descrição |
|-----------|--------|-----------|
| `aim_smooth` | 0.25f | 0.1 = suave, 1.0 = snap imediato |
| `aim_fov` | 350px | Raio do aimbot em pixels |
| Loop delay | 16ms | ~60fps — reduzir para 8ms em 120fps |

Edite em `CheatService.kt` antes de compilar.

---

## Offsets (PER-BUILD — devem ser re-derivados após updates)

Arquivo: `app/src/main/cpp/memory.h`, namespace `FF::`

Consulte o guia completo em `aimbot.cpp` → seção `Offset Hunting Guide`.

Fluxo rápido:
1. `Il2CppDumper` no par `metadata.dat` + `libil2cpp.so` da versão atual
2. Carregue o dump no IDA/Ghidra para encontrar símbolos
3. Ou GameGuardian para derivar por valor (HP scan)

---

## Notas de Evasão

- Serviço aparece como "SystemUI" no gerenciador de apps
- Lib compilada com `-fvisibility=hidden` + strip de símbolos no release
- O overlay é `TYPE_APPLICATION_OVERLAY` — indistinguível de apps legítimos de acessibilidade
- DenyList do Magisk esconde root do processo do FF
- **NÃO use em conta principal** — FF tem detecção server-side de comportamento

---

## Troubleshooting

| Problema | Causa | Fix |
|----------|-------|-----|
| "root ativo?" no log | /proc/mem bloqueado | Verifique Magisk + KernelSU ativo |
| Overlay não aparece | Permissão negada | Configurações → Apps → SystemUI → Permissões |
| Entidades = 0 sempre | Offsets desatualizados | Re-derive via guia |
| Aimbot não move | Accessibility desativado | Reative em Configurações → Acessibilidade |
| FF fecha na hora | DenyList incompleto | Adicione todos os processos do FF ao DenyList |
