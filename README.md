# Translation Package Builder
This plugin is a helper for sending DITA files to translation. It contributes a sub-menu named *Translation Package Builder* in the *DITA Maps Manager*'s contextual menu. The actions present in this group are:
1. *Generate Milestone* - this action is the first one to use. It will generate an unique hash for each documentation resource. This information will be used by the second action to detect which files have been modified. A milestone file should be generated the first time you install this plugin and, afterwards, after each package sent to translators.
2. *Create Modified Files Package* - this action detects which files have been changed since the last generated milestone. These files are packed inside a ZIP file that can be send to translators. After doing this you can also generate a new milestone so that the next package will contain just the new changes.
3. *Apply Package* - when  the translated files arrive from the translator you should open the DITA map that corresponds to the received language (open dita-map-french.ditamap if the package contains the french translation). Invoking this action will extract the changed files inside the map's directory.

How to install
---------
Inside Oxygen, go to Help->Install new add-ons... and paste the following URL:

https://raw.githubusercontent.com/oxygenxml/Translation-Package-Builder/master/build/addon.xml

Follow the installation. After restarting Oxygen, open a DITA map inside the *DITA Maps Manager* and invoke the contextual menu to find the contributed actions.
