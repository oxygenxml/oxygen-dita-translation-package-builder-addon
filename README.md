# Translation Package Builder
This add-on contributes contextual menu actions that help you build a translation package for DITA files that can be sent to translators. You can also extract the changed files back into your project once you receive the package back from the translators.

Once installed, a sub-menu named *Translation Package Builder* is available in contextual menu of the *DITA Maps Manager* with the following actions:
1. *Generate Milestone* - This action is the first one to use. It will generate an unique hash for each documentation resource. This information will be used by the second action to detect which files have been modified. A milestone file should be generated the first time you install this plugin and, afterwards, after each package is sent to translators.
2. *Create Modified Files Package* - This action detects which files have been changed since the last generated milestone. These files are packed inside a ZIP file that can be sent to translators. After doing this, you can also generate a new milestone so that the next package will only contain new changes.
3. *Apply Package* - When  the translated files arrive from the translator you should open the DITA map that corresponds to the received language (open dita-map-french.ditamap if the package contains the french translation). Invoking this action will extract the changed files inside the map's directory.

How to install
--------------
1. In Oxygen, go to **Help->Install new add-ons** to open an add-on selection dialog box.
2. Enter or paste https://raw.githubusercontent.com/oxygenxml/Translation-Package-Builder/master/build/addon.xml in the **Show add-ons from** field.
3. Select the **Translation Package Builder** add-on and click Next.
4. Read the end-user license agreement. Then select the **I accept all terms of the end-user license agreement** option and click **Finish**.
5. Restart the application.

Result: A **Translation Package Builder** submenu will now be available in the contextual menu of the **DITA Maps Manager**. This submenu includes actions to generate a package of modified files that can be sent to translators, as well as an action to extract translated files back into your DITA project.

Video presentation showcasing the **Translation Package Builder** functionality: https://youtu.be/dEWc2HIHvbk?t=1957

Copyright and License
---------------------
Copyright 2018 Syncro Soft SRL.

This project is licensed under [Apache License 2.0](https://github.com/oxygenxml/oxygen-dita-translation-package-builder/blob/master/LICENSE)
