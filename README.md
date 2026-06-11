
### Release & Version Processing

When releasing a new version of Haven:
1. **Update `app/build.gradle.kts`**: bump `versionCode` and `versionName`.
2. **Update `app/src/main/assets/changelog.json`**: add a new entry to the `history` array at the top with the new version, date, and changes. The build script will auto-sync `currentVersion` during the next build.
3. **Push to GitHub and create a release tag** matching the version (e.g. `v1.5.0`). The release notes on GitHub will be auto-fetched by the app on next launch and shown in the Changelog screen.
4. **User Manual**: Automatically generates a "NEW IN vX.X.X" section from `Feature`-tagged changelog entries.
