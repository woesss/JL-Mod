[![GitHub release (latest by date)](https://img.shields.io/github/v/release/woesss/JL-Mod?style=plastic)](https://github.com/woesss/JL-Mod/releases/latest)
[![donate](https://img.shields.io/badge/donate-PayPal-%234D8A99?style=plastic)](https://www.paypal.me/j2meforever)

[Перейти на Русский](README_RU.md)  

Experimental mod of the emulator ["J2ME-Loader" (A J2ME emulator for Android)](https://github.com/nikita36078/J2ME-Loader) with support for games developed for the 3D engine "Mascot Capsule v3"  

<img src="screenshots/screen01.png" width="240"> <img src="screenshots/screen02.png" width="240"> <img src="screenshots/screen03.png" width="240">

### **!!!Attention!!!**
**Some settings have been changed in the mod. J2ME Loader may not work correctly with games, templates and settings installed or configured by the mod and vice versa. In order not to have to reinstall-reconfigure, it is better to make a backup, copy or not specify the same working directory for the mod and J2ME Loader.**

#### **Using shaders (image post-processing filters)**

  Supports the same shader format as [PPSSPP](https://www.ppsspp.org)
  To use, you need to put them in the `shaders` folder in the working directory of the emulator,
  then in the game profile, select the graphics output mode: "Hardware (OpenGL ES)" and select the desired shader.
  Some shaders have settings - when you select one, an icon will appear next to the name, when you click on it, a window with settings will open
  A small collection of compatible shaders can be found in this repository: https://github.com/woesss/ppsspp_shaders

#### **Using sound banks for midi playback (DLS, SF2)**

  Soundbank files (DLS, SF2) should be placed in the `soundbanks` folder in the working directory of the emulator.
  Next, in the game profile settings in the `Audio` section, select the desired one.
  SF2 support is still in beta mode - only standard midi files are supported,
  there are problems with rewinding (after which the sound turns into a cacophony).
  Not all banks are supported by the synthesizers used (Sonivox, TinySoundFont).
  If the bank or audio file is not supported, playback will automatically switch to a standard player with a standard bank.

#### **Mascot Capsule v3 support**
  In some games (seen in "Medal of Honor") the 3D scene may not be displayed due to the overlap with the 2D background.
  Try adding the following line to the "System Properties" field:
  **micro3d.v3.render.no-mix2D3D: true**
  If it doesn't help, please report this game in [bug-report](https://github.com/woesss/JL-Mod/issues/new?assignees=&labels=bug&template=issue-template.md&title=) or in another way.

  Another one property turns on the texture filter (built into OpenGL), but this can generate distortion in the form of extra texels being captured at the edges of polygons:
  **micro3d.v3.texture.filter: true**
   without this setting, the quality of the textures is as close to the original as possible and looks more vintage.

#### **Porting**
  Added the ability to build an Android application from the source code of a J2ME application using the code of this project  
  Read more in the [Wiki](https://github.com/woesss/JL-Mod/wiki/Porting-midlet-instruction)

[Download APK](https://github.com/woesss/JL-Mod/releases/latest)

#### **External links**  
Emulation General Wiki:  
[JL-Mod](http://emulation.gametechwiki.com/index.php/JL-Mod)  
[Mascot Capsule 3D](http://emulation.gametechwiki.com/index.php/Mascot_Capsule_3D)  
[Mascot Capsule 3D compatibility list](https://emulation.gametechwiki.com/index.php/Mascot_Capsule_3D_compatibility_list)  
