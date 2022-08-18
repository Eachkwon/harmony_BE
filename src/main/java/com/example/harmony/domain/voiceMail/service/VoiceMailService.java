package com.example.harmony.domain.voiceMail.service;

import com.example.harmony.domain.user.entity.User;
import com.example.harmony.domain.voiceMail.dto.AllVoiceMailsResponse;
import com.example.harmony.domain.voiceMail.dto.VoiceMailRequest;
import com.example.harmony.domain.voiceMail.entity.VoiceMail;
import com.example.harmony.domain.voiceMail.repository.VoiceMailRepository;
import com.example.harmony.global.s3.S3Service;
import com.example.harmony.global.s3.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VoiceMailService {

    private final VoiceMailRepository voiceMailRepository;

    private final S3Service s3Service;

    public AllVoiceMailsResponse getAllVoiceMails(User user) {
        List<VoiceMail> voiceMails = voiceMailRepository.findAllByFamilyIdOrderByCreatedAtDesc(user.getFamily().getId());
        return new AllVoiceMailsResponse(voiceMails);
    }

    public void createVoiceMail(VoiceMailRequest voiceMailRequest, User user) {
        UploadResponse uploadResponse = s3Service.uploadFile(voiceMailRequest.getSound());
        voiceMailRepository.save(new VoiceMail(voiceMailRequest, uploadResponse, user));
    }

    @Transactional
    public void deleteVoiceMail(Long voiceMailId, User user){
    //삭제
        VoiceMail deleteVoiceMail= voiceMailRepository.findById(voiceMailId)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않은 소리샘입니다."));
        User userId = userRepository.findById(user.getId())
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"등록되지 않은 사용자입니다."));
        String deleteVoiceMailSoundUrl=deleteVoiceMail.getSoundFileName();

        if(!deleteVoiceMail.getUser().getId().equals(userId)) {
            new ResponseStatusException(HttpStatus.FORBIDDEN,"소리샘 삭제 권한이 없습니다");
        } else {
            voiceMailRepository.deleteById(voiceMailId);
            s3Service.deleteFiles(Collections.singletonList(deleteVoiceMailSoundUrl));
        }

    }


}
