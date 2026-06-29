# Changelog

## v0.0.2
- Fix long node names being silently clipped in group view — now ellipsized with "...".
- Fix long group names pushing buttons off-screen — now ellipsized and fills remaining space.
- Dropdown selector now ellipsizes the selected name; popup list shows full names.

## v0.0.1
- Initial Android release workflow.
- Build `libbox` in CI and package the release APK.
- Add signing config support for GitHub Actions secrets.
- Pin the toolchain and build flags for `sing-box` / `libbox` compatibility.
