# 🌉 bridge‑ai

**bridge‑ai** is a terminal bridge that lets you run **Claude Code** and **Codex** with free models via **FreeLLMAPI**. It works on Windows (cmd & PowerShell), macOS, Linux and Termux.

---

## Quick start

```bash
# clone the repo
git clone https://github.com/humanx123/bridge-ai
cd bridge-ai

# install the CLI (adds `bridge-ai` and `ia` to your PATH)
./instalar.sh   # Linux/macOS/Termux
# or on Windows:
instalar.cmd
```

Now run:

```bash
bridge-ai   # interactive menu
ia          # shortcut that runs the same binary
```

## Features

- **Free models** – Uses FreeLLMAPI’s pool of free LLMs (Kilo, OVH, LLM7, Groq, Gemini, OpenRouter, Cerebras, NVIDIA, …). No API keys are required for the default providers.
- **Cross‑platform** – Single binary works everywhere; the installer creates a convenient command (`bridge-ai`) and a shortcut (`ia`).
- **Donations** – BTC, Monero and USDC addresses are shown in the `donar` command, with QR codes rendered directly in the terminal.
- **Configuration** – Settings are stored in `%APPDATA%/bridge-ai` on Windows or `~/.config/bridge-ai` on other OSes.
- **Model selection** – When multiple models are available, the CLI automatically selects the best one (based on the list returned by FreeLLMAPI). You can also pick a specific model via the *Modelos* menu.

---

## Documentation

- **Installation guide** – `instalar.sh`, `instalar.cmd`, `instalar.ps1` (see the repo).
- **API reference** – FreeLLMAPI docs: https://github.com/tashfeenahmed/freellmapi
- **Donate** – Run `bridge-ai donar` to see the QR codes and addresses.

---

## License

MIT © humanx123 (xhumanx & IA)
