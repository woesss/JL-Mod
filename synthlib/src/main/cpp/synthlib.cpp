#include <jni.h>
#include <fluidsynth.h>
#include <unistd.h>
#include "util.h"

class FileRenderer {
public:
    FileRenderer() {
        settings = new_fluid_settings();
        // number of samples as timing source
        fluid_settings_setstr(settings, "player.timing-source", "sample");

        // do not pin the samples
        fluid_settings_setint(settings, "synth.lock-memory", 0);
        synth = new_fluid_synth(settings);
    }

    virtual ~FileRenderer() {
        delete_fluid_synth(synth);
        delete_fluid_settings(settings);
    }

    bool loadSoundFont(const char *soundfontPath) const {
        return fluid_synth_sfload(synth, soundfontPath, 1) != FLUID_FAILED;
    }

    void midiToWav(const char *inputPath, const char *outPath) const {
        fluid_player_t *player;
        fluid_file_renderer_t *renderer;

        fluid_settings_setstr(settings, "audio.file.name", outPath);

        player = new_fluid_player(synth);
        fluid_player_add(player, inputPath);
        fluid_player_play(player);
        renderer = new_fluid_file_renderer(synth);

        renderFile(player, renderer);

        fluid_player_stop(player);
        fluid_player_join(player);
        delete_fluid_file_renderer(renderer);
        delete_fluid_player(player);
    }

    void noteOn(jint channel, jint note) { fluid_synth_noteon(synth, channel, note, 127); }

    void noteOff(jint channel, jint note) { fluid_synth_noteoff(synth, channel, note); }

    void changeInstrument(jint channel, jint instrument) { fluid_synth_program_change(synth, channel, instrument); }

private:
    static void renderFile(fluid_player_t *player, fluid_file_renderer_t *renderer) {
        while (fluid_player_get_status(player) == FLUID_PLAYER_PLAYING) { // NOLINT(altera-unroll-loops)
            if (fluid_file_renderer_process_block(renderer) != FLUID_OK) {
                break;
            }
        }
    }

    fluid_settings_t *settings = nullptr;
    fluid_synth_t *synth = nullptr;
};

extern "C"
JNIEXPORT jlong JNICALL
Java_ru_woesss_synthlib_SynthLib_setupSynth(JNIEnv *env, jobject /*thiz*/, jstring soundfont) {
    auto pFileRenderer = new FileRenderer();
    JniStringPtr soundfontPath(env, soundfont);
    auto success = pFileRenderer->loadSoundFont(soundfontPath.ptr);
    if (success) {
        return reinterpret_cast<jlong>(pFileRenderer);
    }
    delete pFileRenderer;
    auto clazz = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(clazz, "Filed load soundfont");
    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_SynthLib_closeSynth(JNIEnv *, jobject /*thiz*/, jlong handle) {
    auto pFileRenderer = reinterpret_cast<FileRenderer *>(handle);
    delete pFileRenderer;
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_SynthLib_noteOn(JNIEnv *, jobject /*thiz*/, jlong handle, jint channel,
                                        jint note) {
    auto pFileRenderer = reinterpret_cast<FileRenderer *>(handle);
    pFileRenderer->noteOn(channel, note);
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_SynthLib_noteOff(JNIEnv *, jobject /*thiz*/, jlong handle, jint channel,
                                         jint note) {
    auto pFileRenderer = reinterpret_cast<FileRenderer *>(handle);
    pFileRenderer->noteOff(channel, note);
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_SynthLib_changeInstrument(JNIEnv *, jobject /*thiz*/,
                                                  jlong handle,
                                                  jint channel,
                                                  jint instrument) {
    auto pFileRenderer = reinterpret_cast<FileRenderer *>(handle);
    pFileRenderer->changeInstrument(channel, instrument);
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_SynthLib_midiToWav(JNIEnv *env, jobject /*thiz*/,
                                           jlong handle,
                                           jstring input_path,
                                           jstring out_path) {
    auto pFileRenderer = reinterpret_cast<FileRenderer *>(handle);

    JniStringPtr outPath(env, out_path);
    JniStringPtr inputPath(env, input_path);

    pFileRenderer->midiToWav(inputPath.ptr, outPath.ptr);
}