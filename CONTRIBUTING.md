# Contributing

Use JDK 17, Python 3, Pillow 12.0.0 and Android SDK 35. Run the commands in README.md. Never change expected labels, source regions, crop rectangles or negative controls to hide a detector miss. The external set is development data, not an accuracy claim.

Before changing the engine, keep the baseline source frozen. Add a meaningful regression for an observed failure, run the core checks, regenerate the external report, inspect every changed hit/miss/negative flag and commit both report copies. Input hash changes require inspection and a recorded reason. Do not redistribute StockCharts source images.

Native flow tests must use the real UI, Android consent/picker and persistence paths. Synthetic charts verify transport and known geometry; the file-picker flow uses an attributed real market screenshot. Never bypass recognition by injecting a precomputed match. Keep the Studio application ID (`com.lamontlabs.quantravision.studio`) and explicit signing cache identity stable. Publication requires the complete main workflow to pass.
