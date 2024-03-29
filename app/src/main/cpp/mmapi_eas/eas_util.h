//
// Created by woesss on 08.07.2023.
//

#ifndef MMAPI_EAS_UTIL_H
#define MMAPI_EAS_UTIL_H

#include <stdint.h>

namespace mmapi {
    namespace eas {
        static const char *EAS_ERRORS[]{
                "EAS_SUCCESS",
                "EAS_FAILURE",
                "EAS_ERROR_INVALID_MODULE",
                "EAS_ERROR_MALLOC_FAILED",
                "EAS_ERROR_FILE_POS",
                "EAS_ERROR_INVALID_FILE_MODE",
                "EAS_ERROR_FILE_SEEK",
                "EAS_ERROR_FILE_LENGTH",
                "EAS_ERROR_NOT_IMPLEMENTED",
                "EAS_ERROR_CLOSE_FAILED",
                "EAS_ERROR_FILE_OPEN_FAILED",
                "EAS_ERROR_INVALID_HANDLE",
                "EAS_ERROR_NO_MIX_BUFFER",
                "EAS_ERROR_PARAMETER_RANGE",
                "EAS_ERROR_MAX_FILES_OPEN",
                "EAS_ERROR_UNRECOGNIZED_FORMAT",
                "EAS_BUFFER_SIZE_MISMATCH",
                "EAS_ERROR_FILE_FORMAT",
                "EAS_ERROR_SMF_NOT_INITIALIZED",
                "EAS_ERROR_LOCATE_BEYOND_END",
                "EAS_ERROR_INVALID_PCM_TYPE",
                "EAS_ERROR_MAX_PCM_STREAMS",
                "EAS_ERROR_NO_VOICE_ALLOCATED",
                "EAS_ERROR_INVALID_CHANNEL",
                "EAS_ERROR_ALREADY_STOPPED",
                "EAS_ERROR_FILE_READ_FAILED",
                "EAS_ERROR_HANDLE_INTEGRITY",
                "EAS_ERROR_MAX_STREAMS_OPEN",
                "EAS_ERROR_INVALID_PARAMETER",
                "EAS_ERROR_FEATURE_NOT_AVAILABLE",
                "EAS_ERROR_SOUND_LIBRARY",
                "EAS_ERROR_NOT_VALID_IN_THIS_STATE",
                "EAS_ERROR_NO_VIRTUAL_SYNTHESIZER",
                "EAS_ERROR_FILE_ALREADY_OPEN",
                "EAS_ERROR_FILE_ALREADY_CLOSED",
                "EAS_ERROR_INCOMPATIBLE_VERSION",
                "EAS_ERROR_QUEUE_IS_FULL",
                "EAS_ERROR_QUEUE_IS_EMPTY",
                "EAS_ERROR_FEATURE_ALREADY_ACTIVE",
                "EAS_ERROR_DATA_INCONSISTENCY",
        };

        static const char *EAS_FILE_TYPES[]{
                "EAS_FILE_UNKNOWN",
                "EAS_FILE_SMF0",
                "EAS_FILE_SMF1",
                "EAS_FILE_SMAF_UNKNOWN",
                "EAS_FILE_SMAF_MA2",
                "EAS_FILE_SMAF_MA3",
                "EAS_FILE_SMAF_MA5",
                "EAS_FILE_CMX",
                "EAS_FILE_MFI",
                "EAS_FILE_OTA",
                "EAS_FILE_IMELODY",
                "EAS_FILE_RTTTL",
                "EAS_FILE_XMF0",
                "EAS_FILE_XMF1",
                "EAS_FILE_WAVE_PCM",
                "EAS_FILE_WAVE_IMA_ADPCM",
                "EAS_FILE_MMAPI_TONE_CONTROL",
        };

        const char *EAS_GetErrorString(int32_t errorCode);
        const char *EAS_GetFileTypeString(int32_t type);
    } // namespace mmapi
} // namespace eas

#endif //MMAPI_EAS_UTIL_H
