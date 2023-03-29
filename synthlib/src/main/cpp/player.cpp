#include <jni.h>
#include <fluidsynth.h>
#include <unistd.h>
#include "util.h"

#ifndef PLAYER_STATUS_FILED
#define PLAYER_STATUS_FILED -1
#define PLAYER_STATUS_UNREALIZED 0
#define PLAYER_STATUS_REALIZED 1
#define PLAYER_STATUS_PREFETCHED 2
#define PLAYER_STATUS_STARTED 3
#endif

namespace mmapi {
    class Player {
        fluid_settings_t* settings;
        fluid_synth_t* synth;
        fluid_player_t* player;
        fluid_audio_driver_t* audioDriver;

    public:
        Player() {
            settings = new_fluid_settings();
            if (settings == nullptr) {
                status = PLAYER_STATUS_FILED;
                return;
            }
            synth = new_fluid_synth(settings);
            if (synth == nullptr) {
                status = PLAYER_STATUS_FILED;
                return;
            }
            player = new_fluid_player(synth);
            if (player == nullptr) {
                status = PLAYER_STATUS_FILED;
                return;
            }
            /* start the synthesizer thread */
            audioDriver = new_fluid_audio_driver(settings, synth);
            if (audioDriver == nullptr) {
                status = PLAYER_STATUS_FILED;
                return;
            }
            status = PLAYER_STATUS_UNREALIZED;
        }

        virtual ~Player() {
            delete_fluid_audio_driver(audioDriver);
            delete_fluid_player(player);
            delete_fluid_synth(synth);
            delete_fluid_settings(settings);
        }

        bool loadMidiFile(const char *midi) {
            auto success = fluid_player_add(player, midi) != FLUID_FAILED;
            if (success) {
                status = PLAYER_STATUS_PREFETCHED;
            } else {
                status = PLAYER_STATUS_FILED;
            }
            return success;
        }

        bool loadSoundFont(const char *soundfont) {
            auto success = fluid_synth_sfload(synth, soundfont, 1) != FLUID_FAILED;
            if (success) {
                status = PLAYER_STATUS_REALIZED;
            } else {
                status = PLAYER_STATUS_FILED;
            }
            return success;
        }

        bool play() {
            auto success = fluid_player_play(player) != FLUID_FAILED;
            if (success) {
                status = PLAYER_STATUS_STARTED;
            } else {
                status = PLAYER_STATUS_FILED;
            }
            return success;
        }

        void stop() {
            fluid_player_stop(player);
            status = PLAYER_STATUS_PREFETCHED;
        }

        void setLoop(int loop) {
            fluid_player_set_loop(player, loop);
        }

        bool join() {
            fluid_player_join(player);
            auto lStatus = status;
            status = PLAYER_STATUS_PREFETCHED;
            return fluid_player_get_status(player) == FLUID_PLAYER_DONE && lStatus == PLAYER_STATUS_STARTED;
        }

        void reset() {
            stop();
            fluid_player_seek(player, 0);
        }

        int64_t getDuration() {
//            int64_t ticks = fluid_player_get_total_ticks(player);
//            int64_t tempo = fluid_player_get_midi_tempo(player);
//            return ticks * tempo / division;
            // TODO: implement getDuration()
            return -1;
        }

        int64_t setMediaTime(int64_t now) {
            // TODO: implement setMediaTime()
            return -1;
        }

        int64_t getMediaTime() {
            // TODO: implement getMediaTime()
            return -1;
        }

        void setVolume(float l, float r) {
            // TODO: implement setVolume()
        }

        int status;
    };
}

extern "C" JNIEXPORT jlong JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nInit(JNIEnv *env, jobject /*thiz*/, jstring sf_path) {
    JniStringPtr sf(env, sf_path);
    auto player = new mmapi::Player();
    const char *message;
    if (player->status != PLAYER_STATUS_FILED) {
        if (player->loadSoundFont(sf.ptr)) {
            return reinterpret_cast<jlong>(player);
        } else {
            message = "Filed load soundfont";
        }
    } else {
        message = "Filed initialize fluidsynth";
    }
    delete player;
    auto clazz = env->FindClass("java/io/IOException");
    env->ThrowNew(clazz, message);
    return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nPlay(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    return player->play();
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nStop(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    player->stop();
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nSetLoopCount(JNIEnv */*env*/, jobject /*thiz*/,
                                                 jlong handle, jint count) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    player->setLoop(count);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nJoin(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    return player->join();
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nReset(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    player->reset();
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nFinalize(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    player->stop();
    player->join();
    delete player;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nGetDuration(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    return player->getDuration();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nSetMediaTime(JNIEnv */*env*/, jobject /*thiz*/,
                                                 jlong handle, jlong now) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    return player->setMediaTime(now);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nGetMediaTime(JNIEnv */*env*/, jobject /*thiz*/, jlong handle) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    return player->getMediaTime();
}
extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nSetDataSource(JNIEnv *env, jobject /*thiz*/,
                                                  jlong handle, jstring path) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    JniStringPtr mid(env, path);
    if (!player->loadMidiFile(mid.ptr)) {
        auto clazz = env->FindClass("java/io/IOException");
        env->ThrowNew(clazz, "Filed load midi");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_ru_woesss_synthlib_MidiPlayer_nSetVolume(JNIEnv */*env*/, jobject /*thiz*/,
                                              jlong handle, jfloat left, jfloat right) {
    auto player = reinterpret_cast<mmapi::Player *>(handle);
    player->setVolume(left, right);
}