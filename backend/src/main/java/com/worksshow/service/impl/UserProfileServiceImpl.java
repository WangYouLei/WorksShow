package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worksshow.dto.UserProfileDTO;
import com.worksshow.entity.UserExperience;
import com.worksshow.entity.UserProfile;
import com.worksshow.entity.UserSkill;
import com.worksshow.entity.UserWork;
import com.worksshow.mapper.UserExperienceMapper;
import com.worksshow.mapper.UserProfileMapper;
import com.worksshow.mapper.UserSkillMapper;
import com.worksshow.mapper.UserWorkMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户简历档案服务实现
 * <p>
 * 管理用户级档案(user_profile + user_work + user_experience + user_skill),
 * 所有简历模板共享。采用懒创建:首次保存时 INSERT。
 * saveMyProfile 用 @Transactional 保证多表写入原子性。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileMapper userProfileMapper;
    private final UserWorkMapper userWorkMapper;
    private final UserExperienceMapper userExperienceMapper;
    private final UserSkillMapper userSkillMapper;

    /**
     * 获取当前用户档案
     */
    @Override
    public UserProfileDTO getMyProfile() {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询用户档案: userId={}", userId);

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setProfile(toProfileDTO(profile));

        List<UserWork> works = userWorkMapper.selectList(
                new LambdaQueryWrapper<UserWork>()
                        .eq(UserWork::getUserId, userId)
                        .orderByAsc(UserWork::getSortOrder));
        dto.setWorks(works.stream().map(UserProfileServiceImpl::toWorkDTO).toList());

        List<UserExperience> experiences = userExperienceMapper.selectList(
                new LambdaQueryWrapper<UserExperience>()
                        .eq(UserExperience::getUserId, userId)
                        .orderByAsc(UserExperience::getSortOrder));
        dto.setExperiences(experiences.stream().map(UserProfileServiceImpl::toExperienceDTO).toList());

        List<UserSkill> skills = userSkillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>()
                        .eq(UserSkill::getUserId, userId)
                        .orderByAsc(UserSkill::getSortOrder));
        dto.setSkills(skills.stream().map(UserProfileServiceImpl::toSkillDTO).toList());

        return dto;
    }

    /**
     * 保存当前用户档案(整体替换)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO saveMyProfile(UserProfileDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        log.info("保存用户档案: userId={}", userId);

        // 1) profile:懒创建 + 整体更新
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            applyProfile(profile, dto.getProfile());
            userProfileMapper.insert(profile);
            log.info("创建用户档案: userId={}, profileId={}", userId, profile.getId());
        } else {
            applyProfile(profile, dto.getProfile());
            userProfileMapper.updateById(profile);
        }

        // 2) works/experiences/skills:整体替换
        replaceWorks(userId, dto.getWorks());
        replaceExperiences(userId, dto.getExperiences());
        replaceSkills(userId, dto.getSkills());

        return getMyProfile();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将 ProfileDTO 字段映射到 UserProfile 的 p_* 字段
     */
    private void applyProfile(UserProfile profile, UserProfileDTO.ProfileDTO profileDTO) {
        if (profileDTO == null) {
            return;
        }
        profile.setPName(profileDTO.getName());
        profile.setPNameEn(profileDTO.getNameEn());
        profile.setPTitle(profileDTO.getTitle());
        profile.setPTagline(profileDTO.getTagline());
        profile.setPBio(profileDTO.getBio());
        profile.setPEmail(profileDTO.getEmail());
        profile.setPLocation(profileDTO.getLocation());
        profile.setPAvailability(profileDTO.getAvailability());
        profile.setPStats(toStatList(profileDTO.getStats()));
        profile.setPSocials(toSocialList(profileDTO.getSocials()));
    }

    /**
     * 整体替换作品:逻辑删除旧记录 + 批量插入新记录
     */
    private void replaceWorks(Long userId, List<UserProfileDTO.WorkDTO> works) {
        userWorkMapper.delete(new LambdaQueryWrapper<UserWork>()
                .eq(UserWork::getUserId, userId));
        if (works == null || works.isEmpty()) {
            return;
        }
        int sortOrder = 0;
        for (UserProfileDTO.WorkDTO w : works) {
            UserWork work = new UserWork();
            work.setUserId(userId);
            work.setTitle(w.getTitle());
            work.setCategory(w.getCategory());
            work.setCover(w.getCover());
            work.setDescription(w.getDescription());
            work.setYear(w.getYear());
            work.setLink(w.getLink());
            work.setTags(w.getTags());
            // Boolean → Integer(0/1),null 视为 0
            work.setHighlight(Boolean.TRUE.equals(w.getHighlight()) ? 1 : 0);
            work.setSortOrder(sortOrder++);
            userWorkMapper.insert(work);
        }
    }

    /**
     * 整体替换经历:逻辑删除旧记录 + 批量插入新记录
     */
    private void replaceExperiences(Long userId, List<UserProfileDTO.ExperienceDTO> experiences) {
        userExperienceMapper.delete(new LambdaQueryWrapper<UserExperience>()
                .eq(UserExperience::getUserId, userId));
        if (experiences == null || experiences.isEmpty()) {
            return;
        }
        int sortOrder = 0;
        for (UserProfileDTO.ExperienceDTO e : experiences) {
            UserExperience exp = new UserExperience();
            exp.setUserId(userId);
            exp.setPeriod(e.getPeriod());
            exp.setTitle(e.getTitle());
            exp.setOrganization(e.getOrganization());
            exp.setDescription(e.getDescription());
            exp.setType(e.getType());
            exp.setSortOrder(sortOrder++);
            userExperienceMapper.insert(exp);
        }
    }

    /**
     * 整体替换技能:逻辑删除旧记录 + 批量插入新记录
     */
    private void replaceSkills(Long userId, List<UserProfileDTO.SkillDTO> skills) {
        userSkillMapper.delete(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId));
        if (skills == null || skills.isEmpty()) {
            return;
        }
        int sortOrder = 0;
        for (UserProfileDTO.SkillDTO s : skills) {
            UserSkill skill = new UserSkill();
            skill.setUserId(userId);
            skill.setCategory(s.getCategory());
            skill.setCategoryEn(s.getCategoryEn());
            skill.setItems(s.getItems());
            skill.setSortOrder(sortOrder++);
            userSkillMapper.insert(skill);
        }
    }

    // ==================== 实体 → DTO 转换 ====================

    /**
     * UserProfile → ProfileDTO(profile 为 null 时返回空 DTO,前端显示空表单)
     */
    private static UserProfileDTO.ProfileDTO toProfileDTO(UserProfile profile) {
        UserProfileDTO.ProfileDTO dto = new UserProfileDTO.ProfileDTO();
        if (profile == null) {
            return dto;
        }
        dto.setName(profile.getPName());
        dto.setNameEn(profile.getPNameEn());
        dto.setTitle(profile.getPTitle());
        dto.setTagline(profile.getPTagline());
        dto.setBio(profile.getPBio());
        dto.setEmail(profile.getPEmail());
        dto.setLocation(profile.getPLocation());
        dto.setAvailability(profile.getPAvailability());
        dto.setStats(toStatDTOList(profile.getPStats()));
        dto.setSocials(toSocialDTOList(profile.getPSocials()));
        return dto;
    }

    /**
     * UserWork → WorkDTO
     */
    private static UserProfileDTO.WorkDTO toWorkDTO(UserWork work) {
        UserProfileDTO.WorkDTO dto = new UserProfileDTO.WorkDTO();
        dto.setId(work.getId());
        dto.setTitle(work.getTitle());
        dto.setCategory(work.getCategory());
        dto.setCover(work.getCover());
        dto.setDescription(work.getDescription());
        dto.setYear(work.getYear());
        dto.setLink(work.getLink());
        dto.setTags(work.getTags());
        // Integer → Boolean(1 视为 true,其余 false)
        dto.setHighlight(work.getHighlight() != null && work.getHighlight() == 1);
        return dto;
    }

    /**
     * UserExperience → ExperienceDTO
     */
    private static UserProfileDTO.ExperienceDTO toExperienceDTO(UserExperience exp) {
        UserProfileDTO.ExperienceDTO dto = new UserProfileDTO.ExperienceDTO();
        dto.setId(exp.getId());
        dto.setPeriod(exp.getPeriod());
        dto.setTitle(exp.getTitle());
        dto.setOrganization(exp.getOrganization());
        dto.setDescription(exp.getDescription());
        dto.setType(exp.getType());
        return dto;
    }

    /**
     * UserSkill → SkillDTO
     */
    private static UserProfileDTO.SkillDTO toSkillDTO(UserSkill skill) {
        UserProfileDTO.SkillDTO dto = new UserProfileDTO.SkillDTO();
        dto.setId(skill.getId());
        dto.setCategory(skill.getCategory());
        dto.setCategoryEn(skill.getCategoryEn());
        dto.setItems(skill.getItems());
        return dto;
    }

    /**
     * List<StatDTO> → List<Stat>
     */
    private static List<UserProfile.Stat> toStatList(List<UserProfileDTO.StatDTO> stats) {
        if (stats == null) {
            return null;
        }
        List<UserProfile.Stat> list = new ArrayList<>(stats.size());
        for (UserProfileDTO.StatDTO s : stats) {
            UserProfile.Stat stat = new UserProfile.Stat();
            stat.setLabel(s.getLabel());
            stat.setValue(s.getValue());
            list.add(stat);
        }
        return list;
    }

    /**
     * List<SocialDTO> → List<Social>
     */
    private static List<UserProfile.Social> toSocialList(List<UserProfileDTO.SocialDTO> socials) {
        if (socials == null) {
            return null;
        }
        List<UserProfile.Social> list = new ArrayList<>(socials.size());
        for (UserProfileDTO.SocialDTO s : socials) {
            UserProfile.Social social = new UserProfile.Social();
            social.setName(s.getName());
            social.setUrl(s.getUrl());
            social.setIcon(s.getIcon());
            list.add(social);
        }
        return list;
    }

    /**
     * List<Stat> → List<StatDTO>
     */
    private static List<UserProfileDTO.StatDTO> toStatDTOList(List<UserProfile.Stat> stats) {
        if (stats == null) {
            return null;
        }
        List<UserProfileDTO.StatDTO> list = new ArrayList<>(stats.size());
        for (UserProfile.Stat s : stats) {
            UserProfileDTO.StatDTO dto = new UserProfileDTO.StatDTO();
            dto.setLabel(s.getLabel());
            dto.setValue(s.getValue());
            list.add(dto);
        }
        return list;
    }

    /**
     * List<Social> → List<SocialDTO>
     */
    private static List<UserProfileDTO.SocialDTO> toSocialDTOList(List<UserProfile.Social> socials) {
        if (socials == null) {
            return null;
        }
        List<UserProfileDTO.SocialDTO> list = new ArrayList<>(socials.size());
        for (UserProfile.Social s : socials) {
            UserProfileDTO.SocialDTO dto = new UserProfileDTO.SocialDTO();
            dto.setName(s.getName());
            dto.setUrl(s.getUrl());
            dto.setIcon(s.getIcon());
            list.add(dto);
        }
        return list;
    }
}
