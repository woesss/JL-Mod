[![GitHub release (latest by date)](https://img.shields.io/github/v/release/woesss/JL-Mod?style=plastic)](https://github.com/woesss/JL-Mod/releases/latest)
[![donate](https://img.shields.io/badge/donate-PayPal-%234D8A99?style=plastic)](https://www.paypal.me/j2meforever)

[Перейти на Русский](README_RU.md)  

Experimental mod of the emulator ["J2ME-Loader" (A J2ME emulator for Android)](https://github.com/nikita36078/J2ME-Loader) with support for games developed for the 3D engine "Mascot Capsule v3"  

<img src="screenshots/screen01.png" width="240"> <img src="screenshots/screen02.png" width="240"> <img src="screenshots/screen03.png" width="240">

### **!!!Attention!!!**  
**In the mod, some settings are changed. The original "J2ME Loader" may not work correctly with games, templates and settings installed or configured by the mod. In order to not have to reinstall, reconfigure it is better to backup, copy and not specify the "J2ME-Loader" working directory at all.**  

#### **List of Mod changes:**  

- Select a working directory.
- Store app database in a working directory.
- Indicators of the selected button colors in the settings.
- Templates are renamed to "Profiles".
- Assigning any profile as standard (when saving or in the profiles window).
- Adding, editing and set as standard in the window of the profiles list.  
- 1 sec. limit for force close of the midlet.
- Confirmation dialogs when reinstalling existing ones.
- Choice of encoding is transferred to individual settings, a selection button from all supported by the system.
- Buttons shape setting has been moved to the individual, a variant with rounded corners has been added.
- Changed keyboard editing:  
      fixed: drag and drop buttons;  
      added: separate scaling on horizontally and vertically (can be made rectangular or oval).  
- The "System Properties" field has been moved to the end of the settings,  
      unlimit on the number of displayed lines,  
      display of all parameters added by the emulator.  
- Add support of external filters (shaders).  
  Only shaders for [PPSSPP](https://www.ppsspp.org) (same format) are supported  
  To use, you need to put them in the `shaders` folder in the working folder of the emulator,  
  further in the game settings, select the graphics output mode: "HW acceleration (OpenGL ES)" and select the shader of interest  
  A small collection of compatible shaders can be taken in this repository: [ppsspp_shaders](https://github.com/woesss/ppsspp_shaders)  

#### **Support for Mascot Capsule v3 (alpha build):**  
  the implementation is not complete, somewhere a curve, little tested  

Main problems:  
  special effects are partially implemented - therefore, the color rendition may differ from the original.  
  Point sprites (usually used to display the simplest objects) are not fully implemented,  
  if somewhere they are displayed differently than in the original - please inform.  
  In some games (seen in "Medal of Honor") the 3D scene may not be displayed due to overlapping 2D background.  
  Try adding the following line to the System Parameters field:  
  **micro3d.v3.render.no-mix2D3D: true**  
  If it doesn't help, please report this game to [Issues](https://github.com/woesss/JL-Mod/issues/new?assignees=&labels=bug&template=issue-template.md&title=) or another way.  
   
  
   Another one parameter turns on the texture filter (built into OpenGL),  
   but this can generate distortion in the form of capturing extra texels at the edges of polygons:  
   **micro3d.v3.texture.filter: true**  
   without this parameter, the quality of textures is as close as possible to the original and looks more vintage.  
  
[Download APK](https://github.com/woesss/JL-Mod/releases/latest)  
