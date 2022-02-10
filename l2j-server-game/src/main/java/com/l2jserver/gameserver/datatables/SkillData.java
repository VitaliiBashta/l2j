package com.l2jserver.gameserver.datatables;

import com.l2jserver.gameserver.engines.DocumentEngine;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.IXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.l2jserver.gameserver.config.Configuration.general;

@Service
public final class SkillData implements IXmlReader {

  private static final Logger LOG = LoggerFactory.getLogger(SkillData.class);

  private final Map<Integer, Skill> _skills = new HashMap<>();
  private final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
  private final Set<Integer> _enchantable = new HashSet<>();
  private final DocumentEngine documentEngine;

  protected SkillData(DocumentEngine documentEngine) {
    this.documentEngine = documentEngine;
  }

  /**
   * Provides the skill hash
   *
   * @param skill The L2Skill to be hashed
   * @return getSkillHashCode(skill.getId(), skill.getLevel())
   */
  public static int getSkillHashCode(Skill skill) {
    return getSkillHashCode(skill.getId(), skill.getLevel());
  }

  /**
   * Centralized method for easier change of the hashing sys
   *
   * @param skillId The Skill Id
   * @param skillLevel The Skill Level
   * @return The Skill hash number
   */
  public static int getSkillHashCode(int skillId, int skillLevel) {
    return (skillId * 1021) + skillLevel;
  }

  public static SkillData getInstance() {
    return SingletonHolder._instance;
  }

  public void load() {
    final Map<Integer, Skill> _temp = new HashMap<>();
    documentEngine.loadAllSkills(_temp);

    _skills.clear();
    _skills.putAll(_temp);

    _skillMaxLevel.clear();
    _enchantable.clear();
    for (Skill skill : _skills.values()) {
      final int skillId = skill.getId();
      final int skillLvl = skill.getLevel();
      if (skillLvl > 99) {
        _enchantable.add(skillId);
        continue;
      }

      // only non-enchanted skills
      final int maxLvl = getMaxLevel(skillId);
      if (skillLvl > maxLvl) {
        _skillMaxLevel.put(skillId, skillLvl);
      }
    }
  }

  public Skill getSkill(int skillId, int level) {
    final Skill result = _skills.get(getSkillHashCode(skillId, level));
    if (result != null) {
      return result;
    }

    // skill/level not found, fix for transformation scripts
    final int maxLvl = getMaxLevel(skillId);
    // requested level too high
    if ((maxLvl > 0) && (level > maxLvl)) {
      if (general().debug()) {
        LOG.warn(
            "Call to unexisting skill level Id {} requested level {} max level {}!",
            skillId,
            level,
            maxLvl);
      }
      return _skills.get(getSkillHashCode(skillId, maxLvl));
    }

    LOG.warn("No skill info found for skill Id {} and skill level {}!", skillId, level);
    return null;
  }

  public int getMaxLevel(int skillId) {
    final Integer maxLevel = _skillMaxLevel.get(skillId);
    return maxLevel != null ? maxLevel : 0;
  }

  /**
   * Verifies if the given skill ID correspond to an enchantable skill.
   *
   * @param skillId the skill ID
   * @return {@code true} if the skill is enchantable, {@code false} otherwise
   */
  public boolean isEnchantable(int skillId) {
    return _enchantable.contains(skillId);
  }

  /**
   * @param addNoble
   * @param hasCastle
   * @return an array with siege skills. If addNoble == true, will add also Advanced headquarters.
   */
  public Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle) {
    Skill[] temp = new Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
    int i = 0;
    temp[i++] = _skills.get(SkillData.getSkillHashCode(246, 1));
    temp[i++] = _skills.get(SkillData.getSkillHashCode(247, 1));

    if (addNoble) {
      temp[i++] = _skills.get(SkillData.getSkillHashCode(326, 1));
    }
    if (hasCastle) {
      temp[i++] = _skills.get(SkillData.getSkillHashCode(844, 1));
      temp[i] = _skills.get(SkillData.getSkillHashCode(845, 1));
    }
    return temp;
  }

  private static class SingletonHolder {
    protected static final SkillData _instance = new SkillData(null);
  }
}
