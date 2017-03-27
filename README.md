[ ![Discord Support](https://www.mediafire.com/convkey/1f30/84f194magcxff186g.jpg) ](https://discord.gg/p5DAvc6)
[ ![Bugs, Issues, Feature Requests](https://www.mediafire.com/convkey/3860/99n15b2cbgvnp416g.jpg) ](../../issues)
[ ![Donate](https://www.mediafire.com/convkey/3ac7/eurlt0tntrc95zh6g.jpg) ](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=THXHQ5287TBA8)

# SignRanks

Spigot 1.11 plugin to add more control to group changing signs.


# Description
Note: This requires Vault
Allows placing signs that will set a players rank and run various commands afterword.

- Can cost money or be free
- Commands are customizable via config file
- All messages are configurable(Other than error messages)

# Usage

Your sign should look like this:

[SignRanks] (Or the title set in the config)

newGroup

cost

BLANK


# Permissions

https://github.com/bittiez/SignRanks/blob/master/src/plugin.yml


# Installation

- Place the jar file in your plugins folder
- Restart your server


# Configuration

Global configuration is inside the config.yml file, this contains messages and the sign title(default is [SignRanks]) which can all be customized.

Individual signs configuration will be inside the signData.yml file, here you can add commands to run after the players group has been changed.  
There are commands and consoleCommands in this config, the consoleCommands can use [USERNAME] to be replaced with the players name.


# To-Do/Upcoming features

- Add a /signranks id command that will tell you the sign's id you are looking at (For editing the config)
- Add a SignRanks.ignore permission that will make it so that group can't use signrank signs(Mostly for mods and admins so when they break signs they don't change their group also)
