package com.sixcube.recletter.clip.service;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.sixcube.recletter.clip.Repository.ClipRepository;
import com.sixcube.recletter.clip.dto.Clip;
import com.sixcube.recletter.clip.dto.ClipInfo;
import com.sixcube.recletter.clip.exception.*;
import com.sixcube.recletter.studio.S3Util;
import com.sixcube.recletter.studio.StudioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClipServiceImpl implements ClipService {

    private final ClipRepository clipRepository;
    private final StudioUtil studioUtil;
    private final S3Util s3Util;

    private final String videoExtension = ".mp4";


    @Override
    public void createClip(Clip clip, MultipartFile file) {
        if (file==null || !"video/mp4".equals(file.getContentType())) {
            throw new InvalidClipFormatException();
        }
        if (!studioUtil.isStudioParticipant(clip.getStudioId(),clip.getClipOwner())){
            throw new WeirdClipUserException();
        }

        try {
            clipRepository.save(clip);
            System.out.println(clip);

            saveS3Object(clip, file);
        } catch (AmazonS3Exception | IOException e) {
            e.printStackTrace();
            clipRepository.delete(clip); //S3에 저장실패했으므로, DB에만 저장된 것 롤백처리
            throw new SaveClipFailException();
        }
    }

    @Override
    public void updateClip(Clip clip, String prevTitle, MultipartFile file) {
        try {
            System.out.println(clip);
            saveS3Object(clip, file);

            String prevKey = getFileKey(clip.getStudioId(), clip.getClipId(), prevTitle);
            s3Util.deleteObject(prevKey);
            clipRepository.save(clip);
        } catch (AmazonS3Exception | IOException e) {
            e.printStackTrace();
            throw new SaveClipFailException();
        }
    }

    @Override
    public void deleteClip(Clip clip) {
        try {
            String fileKey = getFileKey(clip);

            clipRepository.delete(clip);
            s3Util.deleteObject(fileKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public Clip searchClip(int clipId) {
        return clipRepository.findClipByClipId(clipId).orElseThrow(NoSuchClipException::new);
    }

    @Override
    public String searchClipUrl(int clipId){
        Clip clip=searchClip(clipId);
        return createSignedClipUrl(getFileKey(clip));
    }

//    @Override
    public String searchClipUrl(Clip clip){
        return createSignedClipUrl(getFileKey(clip));
    }
    @Override
    public String getFileKey(Clip clip) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(clip.getStudioId())
                .append("/").append(clip.getClipId())
                .append("/").append(clip.getClipTitle())
                .append(videoExtension);
        return stringBuilder.toString();
    }

    public String getFileKey(String studioId, int clipId, String clipTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(studioId)
                .append("/").append(clipId)
                .append("/").append(clipTitle)
                .append(videoExtension);
        return stringBuilder.toString();
    }

    public void saveS3Object(Clip clip, MultipartFile file) throws IOException,AmazonS3Exception {
        String fileKey= getFileKey(clip);
        System.out.println(fileKey);
        s3Util.saveObject(fileKey,file);
    }

    @Override
    public Clip searchMyClip(String userId, int clipId) throws NotClipOwnerException{
        Clip clip=searchClip(clipId);
        if(userId.equals(clip.getClipOwner())){
            return clip;
        } else{
          throw new NotClipOwnerException();
        }
    }

    @Override
    public List<ClipInfo> searchClipInfoList(String studioId) {
        List<Clip> clipList = clipRepository.findClipsByStudioId(studioId);
        List<ClipInfo> clipInfoList = new ArrayList<>();
        for (Clip clip : clipList) {
            ClipInfo clipInfo = ClipInfo.builder()
                    .clipId(clip.getClipId())
                    .clipTitle(clip.getClipTitle())
                    .clipVolume(clip.getClipVolume())
                    .clipOwner(clip.getClipOwner())
                    .clipContent(clip.getClipContent())
                    .clipOrder(clip.getClipOrder())
                    .clipUrl(createSignedClipUrl(getFileKey(clip)))
                    .build();
            clipInfoList.add(clipInfo);
        }
        return clipInfoList;
    }

    public String createSignedClipUrl(String fileName){
        String url="";
        try {
            url= s3Util.getSignedUrl(fileName);
        } catch (Exception e) {
            System.out.println("TT...");
            e.printStackTrace();
            throw new AwsAuthorizationException();
        }
        return url;
    }


}
