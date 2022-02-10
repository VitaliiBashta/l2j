package com.l2jserver.gameserver.datatables;

import com.l2jserver.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.handler.EffectHandler;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.base.PlayerState;
import com.l2jserver.gameserver.model.conditions.*;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.interfaces.Identifiable;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.type.ArmorType;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.EffectScope;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.stats.functions.FuncTemplate;
import com.l2jserver.gameserver.util.IXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;

@Service
public class SkillData extends IXmlReader {

  private static final Logger LOG = LoggerFactory.getLogger(SkillData.class);
  protected final Map<String, String[]> tables = new HashMap<>();
  private final List<File> _skillFiles = new ArrayList<>();
  private final List<Skill> skillsInFile = new ArrayList<>();
  private final Map<Integer, Skill> _skills = new HashMap<>();
  private final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
  private final Set<Integer> _enchantable = new HashSet<>();
  private final EffectHandler effectHandler;
  private File file;
  private SkillInfo currentSkill;
  public SkillData(EffectHandler effectHandler) {
    this.effectHandler = effectHandler;
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
    parseDatapackDirectory("data/stats/skills", true);
    final Map<Integer, Skill> _temp = new HashMap<>();
//    loadAllSkills(_temp);

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

  @Override
  public void parseDocument(Document doc) {
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if ("skill".equalsIgnoreCase(d.getNodeName())) {
              setCurrentSkill(new SkillInfo());
              parseSkill(d);
              skillsInFile.addAll(currentSkill.skills);
              resetTable();
            }
          }
        } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
          setCurrentSkill(new SkillInfo());
          parseSkill(n);
          skillsInFile.addAll(currentSkill.skills);
        }
      }
  }

  protected void resetTable() {
    tables.clear();
  }

  private void setCurrentSkill(SkillInfo skill) {
    currentSkill = skill;
  }

  protected void setTable(String name, String[] table) {
    tables.put(name, table);
  }

  protected void parseTable(Node n) {
    NamedNodeMap attrs = n.getAttributes();
    String name = attrs.getNamedItem("name").getNodeValue();
    if (name.charAt(0) != '#') {
      throw new IllegalArgumentException("Table name must start with #");
    }
    StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
    List<String> array = new ArrayList<>(data.countTokens());
    while (data.hasMoreTokens()) {
      array.add(data.nextToken());
    }
    setTable(name, array.toArray(new String[array.size()]));
  }

  protected void parseSkill(Node n) {
    NamedNodeMap attrs = n.getAttributes();
    int enchantLevels1 = 0;
    int enchantLevels2 = 0;
    int enchantLevels3 = 0;
    int enchantLevels4 = 0;
    int enchantLevels5 = 0;
    int enchantLevels6 = 0;
    int enchantLevels7 = 0;
    int enchantLevels8 = 0;
    int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
    String skillName = attrs.getNamedItem("name").getNodeValue();
    String levels = attrs.getNamedItem("levels").getNodeValue();
    int lastLvl = Integer.parseInt(levels);
    if (attrs.getNamedItem("enchantGroup1") != null) {
      enchantLevels1 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              1,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup1").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup2") != null) {
      enchantLevels2 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              2,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup2").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup3") != null) {
      enchantLevels3 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              3,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup3").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup4") != null) {
      enchantLevels4 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              4,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup4").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup5") != null) {
      enchantLevels5 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              5,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup5").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup6") != null) {
      enchantLevels6 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              6,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup6").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup7") != null) {
      enchantLevels7 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              7,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup7").getNodeValue()));
    }
    if (attrs.getNamedItem("enchantGroup8") != null) {
      enchantLevels8 =
              EnchantSkillGroupsData.getInstance()
                      .addNewRouteForSkill(
                              skillId,
                              lastLvl,
                              8,
                              Integer.parseInt(attrs.getNamedItem("enchantGroup8").getNodeValue()));
    }

    currentSkill.id = skillId;
    currentSkill.name = skillName;
    currentSkill.sets = new StatsSet[lastLvl];
    currentSkill.enchsets1 = new StatsSet[enchantLevels1];
    currentSkill.enchsets2 = new StatsSet[enchantLevels2];
    currentSkill.enchsets3 = new StatsSet[enchantLevels3];
    currentSkill.enchsets4 = new StatsSet[enchantLevels4];
    currentSkill.enchsets5 = new StatsSet[enchantLevels5];
    currentSkill.enchsets6 = new StatsSet[enchantLevels6];
    currentSkill.enchsets7 = new StatsSet[enchantLevels7];
    currentSkill.enchsets8 = new StatsSet[enchantLevels8];

    for (int i = 0; i < lastLvl; i++) {
      currentSkill.sets[i] = new StatsSet();
      currentSkill.sets[i].set("skill_id", currentSkill.id);
      currentSkill.sets[i].set("level", i + 1);
      currentSkill.sets[i].set("name", currentSkill.name);
    }

    if (currentSkill.sets.length != lastLvl) {
      throw new RuntimeException(
              "Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
    }

    Node first = n.getFirstChild();
    for (n = first; n != null; n = n.getNextSibling()) {
      if ("table".equalsIgnoreCase(n.getNodeName())) {
        parseTable(n);
      }
    }
    for (int i = 1; i <= lastLvl; i++) {
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          // Extractable item skills by Zoey76
          if ("capsuled_items_skill"
                  .equalsIgnoreCase(n.getAttributes().getNamedItem("name").getNodeValue())) {
            setExtractableSkillData(
                    currentSkill.sets[i - 1], getTableValue("#extractableItems", i));
          } else {
            parseBeanSet(n, currentSkill.sets[i - 1], i);
          }
        }
      }
    }
    for (int i = 0; i < enchantLevels1; i++) {
      currentSkill.enchsets1[i] = new StatsSet();
      currentSkill.enchsets1[i].set("skill_id", currentSkill.id);
      // currentSkill.enchsets1[i] = currentSkill.sets[currentSkill.sets.length-1];
      currentSkill.enchsets1[i].set("level", i + 101);
      currentSkill.enchsets1[i].set("name", currentSkill.name);
      // currentSkill.enchsets1[i].set("skillType", "NOTDONE");

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets1[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant1".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets1[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets1.length != enchantLevels1) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels1
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels2; i++) {
      currentSkill.enchsets2[i] = new StatsSet();
      // currentSkill.enchsets2[i] = currentSkill.sets[currentSkill.sets.length-1];
      currentSkill.enchsets2[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets2[i].set("level", i + 201);
      currentSkill.enchsets2[i].set("name", currentSkill.name);
      // currentSkill.enchsets2[i].set("skillType", "NOTDONE");

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets2[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant2".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets2[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets2.length != enchantLevels2) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels2
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels3; i++) {
      currentSkill.enchsets3[i] = new StatsSet();
      currentSkill.enchsets3[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets3[i].set("level", i + 301);
      currentSkill.enchsets3[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets3[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant3".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets3[i], i + 1);
        }
      }
    }


    if (currentSkill.enchsets3.length != enchantLevels3) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels3
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels4; i++) {
      currentSkill.enchsets4[i] = new StatsSet();
      currentSkill.enchsets4[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets4[i].set("level", i + 401);
      currentSkill.enchsets4[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets4[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant4".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets4[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets4.length != enchantLevels4) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels4
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels5; i++) {
      currentSkill.enchsets5[i] = new StatsSet();
      currentSkill.enchsets5[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets5[i].set("level", i + 501);
      currentSkill.enchsets5[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets5[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant5".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets5[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets5.length != enchantLevels5) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels5
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels6; i++) {
      currentSkill.enchsets6[i] = new StatsSet();
      currentSkill.enchsets6[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets6[i].set("level", i + 601);
      currentSkill.enchsets6[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets6[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant6".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets6[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets6.length != enchantLevels6) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels6
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels7; i++) {
      currentSkill.enchsets7[i] = new StatsSet();
      currentSkill.enchsets7[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets7[i].set("level", i + 701);
      currentSkill.enchsets7[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets7[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant7".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets7[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets7.length != enchantLevels7) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels7
                      + " levels expected");
    }

    for (int i = 0; i < enchantLevels8; i++) {
      currentSkill.enchsets8[i] = new StatsSet();
      currentSkill.enchsets8[i].set("skill_id", currentSkill.id);
      currentSkill.enchsets8[i].set("level", i + 801);
      currentSkill.enchsets8[i].set("name", currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets8[i], currentSkill.sets.length);
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant8".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, currentSkill.enchsets8[i], i + 1);
        }
      }
    }

    if (currentSkill.enchsets8.length != enchantLevels8) {
      throw new RuntimeException(
              "Skill id="
                      + skillId
                      + " number of levels missmatch, "
                      + enchantLevels8
                      + " levels expected");
    }

    makeSkills();
    for (int i = 0; i < lastLvl; i++) {
      currentSkill.currentLevel = i;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("cond".equalsIgnoreCase(n.getNodeName())) {
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("effects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("startEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("pveEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("endEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("selfEffects".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
    }

    for (int i = lastLvl; i < (lastLvl + enchantLevels1); i++) {
      currentSkill.currentLevel = i - lastLvl;
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant1cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant1Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant1startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant1channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant1pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant1pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant1endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant1selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i = lastLvl + enchantLevels1; i < (lastLvl + enchantLevels1 + enchantLevels2); i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel = i - lastLvl - enchantLevels1;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant2cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant2Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant2startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant2channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant2pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant2pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant2endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant2selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i = lastLvl + enchantLevels1 + enchantLevels2;
         i < (lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel = i - lastLvl - enchantLevels1 - enchantLevels2;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant3cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant3Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant3startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant3channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant3pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant3pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant3endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant3selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3;
         i < (lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel = i - lastLvl - enchantLevels1 - enchantLevels2 - enchantLevels3;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant4cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant4Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant4startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant4channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant4pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant4pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant4endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant4selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4;
         i
                 < (lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel =
              i - lastLvl - enchantLevels1 - enchantLevels2 - enchantLevels3 - enchantLevels4;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant5cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant5Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant5startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant5channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant5pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant5pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant5endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant5selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i =
         lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5;
         i
                 < (lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5
                 + enchantLevels6);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel =
              i
                      - lastLvl
                      - enchantLevels1
                      - enchantLevels2
                      - enchantLevels3
                      - enchantLevels4
                      - enchantLevels5;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant6cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant6Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant6startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant6channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant6pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant6pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant6endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant6selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i =
         lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5
                 + enchantLevels6;
         i
                 < (lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5
                 + enchantLevels6
                 + enchantLevels7);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel =
              i
                      - lastLvl
                      - enchantLevels1
                      - enchantLevels2
                      - enchantLevels3
                      - enchantLevels4
                      - enchantLevels5
                      - enchantLevels6;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant7cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant7Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant7startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant7channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant7pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant7pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant7endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant7selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundChannelingEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    for (int i =
         lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5
                 + enchantLevels6
                 + enchantLevels7;
         i
                 < (lastLvl
                 + enchantLevels1
                 + enchantLevels2
                 + enchantLevels3
                 + enchantLevels4
                 + enchantLevels5
                 + enchantLevels6
                 + enchantLevels7
                 + enchantLevels8);
         i++) {
      boolean foundCond = false,
              foundEffect = false,
              foundChannelingEffects = false,
              foundStartEffects = false,
              foundPveEffects = false,
              foundPvpEffects = false,
              foundEndEffects = false,
              foundSelfEffects = false;
      currentSkill.currentLevel =
              i
                      - lastLvl
                      - enchantLevels1
                      - enchantLevels2
                      - enchantLevels3
                      - enchantLevels4
                      - enchantLevels5
                      - enchantLevels6
                      - enchantLevels7;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant8cond".equalsIgnoreCase(n.getNodeName())) {
          foundCond = true;
          Condition condition =
                  parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          Node msgId = n.getAttributes().getNamedItem("msgId");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          } else if ((condition != null) && (msgId != null)) {
            condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
            Node addName = n.getAttributes().getNamedItem("addName");
            if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
              condition.addName();
            }
          }
          currentSkill.currentSkills.get(i).attach(condition, false);
        } else if ("enchant8Effects".equalsIgnoreCase(n.getNodeName())) {
          foundEffect = true;
          parseTemplate(n, currentSkill.currentSkills.get(i));
        } else if ("enchant8startEffects".equalsIgnoreCase(n.getNodeName())) {
          foundStartEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
        } else if ("enchant8channelingEffects".equalsIgnoreCase(n.getNodeName())) {
          foundChannelingEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
        } else if ("enchant8pveEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPveEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
        } else if ("enchant8pvpEffects".equalsIgnoreCase(n.getNodeName())) {
          foundPvpEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
        } else if ("enchant8endEffects".equalsIgnoreCase(n.getNodeName())) {
          foundEndEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
        } else if ("enchant8selfEffects".equalsIgnoreCase(n.getNodeName())) {
          foundSelfEffects = true;
          parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
        }
      }
      // If none found, the enchanted skill will take effects from maxLvL of norm skill
      if (!foundCond
              || !foundEffect
              || !foundChannelingEffects
              || !foundStartEffects
              || !foundPveEffects
              || !foundPvpEffects
              || !foundEndEffects
              || !foundSelfEffects) {
        currentSkill.currentLevel = lastLvl - 1;
        for (n = first; n != null; n = n.getNextSibling()) {
          if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition =
                    parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            } else if ((condition != null) && (msgId != null)) {
              condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
              Node addName = n.getAttributes().getNamedItem("addName");
              if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
                condition.addName();
              }
            }
            currentSkill.currentSkills.get(i).attach(condition, false);
          } else if (!foundEffect && "effects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i));
          } else if (!foundStartEffects && "startEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.START);
          } else if (!foundChannelingEffects
                  && "channelingEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.CHANNELING);
          } else if (!foundPveEffects && "pveEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVE);
          } else if (!foundPvpEffects && "pvpEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.PVP);
          } else if (!foundEndEffects && "endEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.END);
          } else if (!foundSelfEffects && "selfEffects".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, currentSkill.currentSkills.get(i), EffectScope.SELF);
          }
        }
      }
    }
    currentSkill.skills.addAll(currentSkill.currentSkills);
  }


//  public void loadAllSkills(final Map<Integer, Skill> allSkills) {
//    int count = 0;
//    for (File file : _skillFiles) {
//      List<Skill> s = loadSkills(file);
//      if (s == null) {
//        continue;
//      }
//      for (Skill skill : s) {
//        allSkills.put(SkillData.getSkillHashCode(skill), skill);
//        count++;
//      }
//    }
//    LOG.info("Loaded {} skill templates from XML files.", count);
//  }


//  public List<Skill> loadSkills(File file) {
//    if (file == null) {
//      LOG.warn("Skill file not found!");
//      return null;
//    }
//    DocumentSkill doc = new DocumentSkill(file);
//    doc.parse();
//    return doc.getSkills();
//  }

  public Skill getSkill(int skillId, int level) {
    final Skill result = _skills.get(getSkillHashCode(skillId, level));
    if (result != null) {
      return result;
    }

    // skill/level not found, fix for transformation scripts
    final int maxLvl = getMaxLevel(skillId);
    // requested level too high
    if ((maxLvl > 0) && (level > maxLvl)) {
      return _skills.get(getSkillHashCode(skillId, maxLvl));
    }

    throw new IllegalArgumentException("No skill info found for skill Id "+skillId+" and skill level " + level);
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

  protected void parseBeanSet(Node n, StatsSet set, Integer level) {
    String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
    String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
    char ch = value.isEmpty() ? ' ' : value.charAt(0);
    if ((ch == '#') || (ch == '-') || Character.isDigit(ch)) {
      set.set(name, String.valueOf(getValue(value, level)));
    } else {
      set.set(name, value);
    }
  }

  protected String getTableValue(String name) {
    return tables.get(name)[currentSkill.currentLevel];
  }

  protected String getTableValue(String name, int idx) {
    try {
      return tables.get(name)[idx - 1];
    } catch (RuntimeException e) {
      LOG.error(

              "wrong level count in skill Id " + currentSkill.id + " name: " + name + " index : " + idx,
              e);
      return "";
    }
  }

  protected Condition parseLogicAnd(Node n, Object template) {
    ConditionLogicAnd cond = new ConditionLogicAnd();
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        cond.add(parseCondition(n, template));
      }
    }
    if ((cond.conditions == null) || (cond.conditions.length == 0)) {
      LOG.error("Empty <and> condition in " + file);
    }
    return cond;
  }

  protected Condition parseLogicOr(Node n, Object template) {
    ConditionLogicOr cond = new ConditionLogicOr();
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        cond.add(parseCondition(n, template));
      }
    }
    if ((cond.conditions == null) || (cond.conditions.length == 0)) {
      LOG.error("Empty <or> condition in " + file);
    }
    return cond;
  }


  protected Condition joinAnd(Condition cond, Condition c) {
    if (cond == null) {
      return c;
    }
    if (cond instanceof ConditionLogicAnd) {
      ((ConditionLogicAnd) cond).add(c);
      return cond;
    }
    ConditionLogicAnd and = new ConditionLogicAnd();
    and.add(cond);
    and.add(c);
    return and;
  }

  protected Condition parsePlayerCondition(Node n, Object template) {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node a = attrs.item(i);
      switch (a.getNodeName().toLowerCase()) {
        case "races" -> {
          final String[] racesVal = a.getNodeValue().split(",");
          final Race[] races = new Race[racesVal.length];
          for (int r = 0; r < racesVal.length; r++) {
            if (racesVal[r] != null) {
              races[r] = Race.valueOf(racesVal[r]);
            }
          }
          cond = joinAnd(cond, new ConditionPlayerRace(races));
        }
        case "level" -> {
          int lvl = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
        }
        case "levelrange" -> {
          String[] range = getValue(a.getNodeValue(), template).split(";");
          if (range.length == 2) {
            final int minimumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
            final int maximumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
            cond = joinAnd(cond, new ConditionPlayerLevelRange(minimumLevel, maximumLevel));
          }
        }
        case "resting" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
        }
        case "flying" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
        }
        case "moving" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
        }
        case "running" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
        }
        case "standing" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
        }
        case "behind" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
        }
        case "front" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
        }
        case "chaotic" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
        }
        case "olympiad" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
        }
        case "ishero" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerIsHero(val));
        }
        case "transformationid" -> {
          int id = Integer.parseInt(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerTransformationId(id));
        }
        case "hp" -> {
          int hp = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerHp(hp));
        }
        case "mp" -> {
          int hp = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerMp(hp));
        }
        case "cp" -> {
          int cp = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerCp(cp));
        }
        case "grade" -> {
          int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerGrade(expIndex));
        }
        case "pkcount" -> {
          int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
        }
        case "siegezone" -> {
          int value = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionSiegeZone(value, true));
        }
        case "siegeside" -> {
          int value = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerSiegeSide(value));
        }
        case "charges" -> {
          int value = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerCharges(value));
        }
        case "souls" -> {
          int value = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerSouls(value));
        }
        case "weight" -> {
          int weight = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerWeight(weight));
        }
        case "invsize" -> {
          int size = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerInvSize(size));
        }
        case "isclanleader" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerIsClanLeader(val));
        }
        case "ontvtevent" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerTvTEvent(val));
        }
        case "pledgeclass" -> {
          int pledgeClass = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
        }
        case "clanhall" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          ArrayList<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
        }
        case "fort" -> {
          int fort = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerHasFort(fort));
        }
        case "castle" -> {
          int castle = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
        }
        case "sex" -> {
          int sex = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionPlayerSex(sex));
        }
        case "flymounted" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerFlyMounted(val));
        }
        case "vehiclemounted" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerVehicleMounted(val));
        }
        case "landingzone" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerLandingZone(val));
        }
        case "active_effect_id" -> {
          int effectId = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effectId));
        }
        case "active_effect_id_lvl" -> {
          String val = getValue(a.getNodeValue(), template);
          int effect_id = Integer.decode(getValue(val.split(",")[0], template));
          int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
          cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
        }
        case "active_skill_id" -> {
          int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
        }
        case "active_skill_id_lvl" -> {
          String val = getValue(a.getNodeValue(), template);
          int skill_id = Integer.decode(getValue(val.split(",")[0], template));
          int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
          cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
        }
        case "class_id_restriction" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          ArrayList<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
        }
        case "subclass" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerSubclass(val));
        }
        case "instanceid" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          ArrayList<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionPlayerInstanceId(array));
        }
        case "agathionid" -> {
          int agathionId = Integer.decode(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
        }
        case "cloakstatus" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionPlayerCloakStatus(val));
        }
        case "haspet" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          ArrayList<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionPlayerHasPet(array));
        }
        case "hasservitor" -> cond = joinAnd(cond, new ConditionPlayerHasServitor());
        case "npcidradius" -> {
          final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          if (st.countTokens() == 3) {
            final String[] ids = st.nextToken().split(";");
            final int[] npcIds = new int[ids.length];
            for (int index = 0; index < ids.length; index++) {
              npcIds[index] = Integer.parseInt(getValue(ids[index], template));
            }
            final int radius = Integer.parseInt(st.nextToken());
            final boolean val = Boolean.parseBoolean(st.nextToken());
            cond = joinAnd(cond, new ConditionPlayerRangeFromNpc(npcIds, radius, val));
          }
        }
        case "callpc" -> cond = joinAnd(cond, new ConditionPlayerCallPc(Boolean.parseBoolean(a.getNodeValue())));
        case "cancreatebase" -> cond = joinAnd(cond, new ConditionPlayerCanCreateBase(Boolean.parseBoolean(a.getNodeValue())));
        case "cancreateoutpost" -> cond = joinAnd(cond, new ConditionPlayerCanCreateOutpost(Boolean.parseBoolean(a.getNodeValue())));
        case "canescape" -> cond = joinAnd(cond, new ConditionPlayerCanEscape(Boolean.parseBoolean(a.getNodeValue())));
        case "canrefuelairship" -> cond = joinAnd(cond, new ConditionPlayerCanRefuelAirship(Integer.parseInt(a.getNodeValue())));
        case "canresurrect" -> cond = joinAnd(cond, new ConditionPlayerCanResurrect(Boolean.parseBoolean(a.getNodeValue())));
        case "cansummon" -> cond = joinAnd(cond, new ConditionPlayerCanSummon(Boolean.parseBoolean(a.getNodeValue())));
        case "cansummonsiegegolem" -> cond = joinAnd(cond, new ConditionPlayerCanSummonSiegeGolem(Boolean.parseBoolean(a.getNodeValue())));
        case "cansweep" -> cond = joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
        case "cantakecastle" -> cond = joinAnd(cond, new ConditionPlayerCanTakeCastle());
        case "cantakefort" -> cond = joinAnd(cond, new ConditionPlayerCanTakeFort(Boolean.parseBoolean(a.getNodeValue())));
        case "cantransform" -> cond = joinAnd(cond, new ConditionPlayerCanTransform(Boolean.parseBoolean(a.getNodeValue())));
        case "canuntransform" -> cond = joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.parseBoolean(a.getNodeValue())));
        case "insidezoneid" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          List<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionPlayerInsideZoneId(array));
        }
        case "checkabnormal" -> {
          final String value = a.getNodeValue();
          if (value.contains(";")) {
            final String[] values = value.split(";");
            final var type = AbnormalType.valueOf(values[0]);
            final var level = Integer.decode(getValue(values[1], template));
            final var mustHave = Boolean.parseBoolean(values[2]);
            cond = joinAnd(cond, new ConditionCheckAbnormal(type, level, mustHave));
          } else {
            final var level = Integer.decode(getValue(value, template));
            cond = joinAnd(cond, new ConditionCheckAbnormal(AbnormalType.valueOf(value), level, true));
          }
          break;
        }
        case "categorytype" -> {
          final String[] values = a.getNodeValue().split(",");
          final Set<CategoryType> array = new HashSet<>(values.length);
          for (String value : values) {
            array.add(CategoryType.valueOf(getValue(value, null)));
          }
          cond = joinAnd(cond, new ConditionCategoryType(array));
        }
        case "hasagathion" -> cond = joinAnd(cond, new ConditionPlayerHasAgathion(Boolean.parseBoolean(a.getNodeValue())));
        case "agathionenergy" -> cond = joinAnd(cond, new ConditionPlayerAgathionEnergy(Integer.decode(getValue(a.getNodeValue(), null))));
        default -> LOG.error("Unrecognized <player> condition " + a.getNodeName().toLowerCase() + " in " + file);
      }
    }

    if (cond == null) {
      LOG.error("Unrecognized <player> condition in " + file);
    }
    return cond;
  }

  protected Condition parseUsingCondition(Node n) {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node a = attrs.item(i);
      switch (a.getNodeName().toLowerCase()) {
        case "kind" -> {
          int mask = 0;
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          while (st.hasMoreTokens()) {
            int old = mask;
            String item = st.nextToken().trim();
            for (WeaponType wt : WeaponType.values()) {
              if (wt.name().equals(item)) {
                mask |= wt.mask();
              }
            }

            for (ArmorType at : ArmorType.values()) {
              if (at.name().equals(item)) {
                mask |= at.mask();
              }
            }

            if (old == mask) {
              LOG.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
            }
          }
          cond = joinAnd(cond, new ConditionUsingItemType(mask));
        }
        case "slot" -> {
          int mask = 0;
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          while (st.hasMoreTokens()) {
            int old = mask;
            String item = st.nextToken().trim();
            if (ItemTable.SLOTS.containsKey(item)) {
              mask |= ItemTable.SLOTS.get(item);
            }

            if (old == mask) {
              LOG.info("[parseUsingCondition=\"slot\"] Unknown item slot name: " + item);
            }
          }
          cond = joinAnd(cond, new ConditionUsingSlotType(mask));
        }
        case "skill" -> {
          int id = Integer.parseInt(a.getNodeValue());
          cond = joinAnd(cond, new ConditionUsingSkill(id));
        }
        case "slotitem" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
          int id = Integer.parseInt(st.nextToken().trim());
          int slot = Integer.parseInt(st.nextToken().trim());
          int enchant = 0;
          if (st.hasMoreTokens()) {
            enchant = Integer.parseInt(st.nextToken().trim());
          }
          cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
        }
        case "weaponchange" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionChangeWeapon(val));
        }
      }
    }

    if (cond == null) {
      LOG.error("Unrecognized <using> condition in " + file);
    }
    return cond;
  }

  protected Condition parseLogicNot(Node n, Object template) {
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        return new ConditionLogicNot(parseCondition(n, template));
      }
    }
    LOG.error("Empty <not> condition in " + file);
    return null;
  }
  protected Condition parseCondition(Node n, Object template) {
    while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE)) {
      n = n.getNextSibling();
    }

    Condition condition = null;
    if (n != null) {
      switch (n.getNodeName().toLowerCase()) {
        case "and" -> condition = parseLogicAnd(n, template);
        case "or" -> condition = parseLogicOr(n, template);
        case "not" -> condition = parseLogicNot(n, template);
        case "player" -> condition = parsePlayerCondition(n, template);
        case "target" -> condition = parseTargetCondition(n, template);
        case "using" -> condition = parseUsingCondition(n);
        case "game" -> condition = parseGameCondition(n);
      }
    }
    return condition;
  }

  protected Condition parseGameCondition(Node n) {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node a = attrs.item(i);
      if ("skill".equalsIgnoreCase(a.getNodeName())) {
        boolean val = Boolean.parseBoolean(a.getNodeValue());
        cond = joinAnd(cond, new ConditionWithSkill(val));
      }
      if ("night".equalsIgnoreCase(a.getNodeName())) {
        boolean val = Boolean.parseBoolean(a.getNodeValue());
        cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, val));
      }
      if ("chance".equalsIgnoreCase(a.getNodeName())) {
        int val = Integer.decode(getValue(a.getNodeValue(), null));
        cond = joinAnd(cond, new ConditionGameChance(val));
      }
    }
    if (cond == null) {
      LOG.error("Unrecognized <game> condition in " + file);
    }
    return cond;
  }
  protected Condition parseTargetCondition(Node n, Object template) {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node a = attrs.item(i);
      switch (a.getNodeName().toLowerCase()) {
        case "aggro" -> {
          boolean val = Boolean.parseBoolean(a.getNodeValue());
          cond = joinAnd(cond, new ConditionTargetAggro(val));
        }
        case "siegezone" -> {
          int value = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionSiegeZone(value, false));
        }
        case "level" -> {
          int lvl = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionTargetLevel(lvl));
        }
        case "levelrange" -> {
          String[] range = getValue(a.getNodeValue(), template).split(";");
          if (range.length == 2) {
            int minimumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
            int maximumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
            cond = joinAnd(cond, new ConditionTargetLevelRange(minimumLevel, maximumLevel));
          }
        }
        case "myparty" -> cond = joinAnd(cond, new ConditionTargetMyParty(a.getNodeValue()));
        case "playable" -> cond = joinAnd(cond, new ConditionTargetPlayable());
        case "class_id_restriction" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          List<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
        }
        case "active_effect_id" -> {
          int effectId = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionTargetActiveEffectId(effectId));
        }
        case "active_effect_id_lvl" -> {
          String val = getValue(a.getNodeValue(), template);
          int effect_id = Integer.decode(getValue(val.split(",")[0], template));
          int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
          cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
        }
        case "active_skill_id" -> {
          int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
        }
        case "active_skill_id_lvl" -> {
          String val = getValue(a.getNodeValue(), template);
          int skill_id = Integer.decode(getValue(val.split(",")[0], template));
          int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
          cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
        }
        case "abnormal" -> {
          int abnormalId = Integer.decode(getValue(a.getNodeValue(), template));
          cond = joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
        }
        case "mindistance" -> {
          int distance = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionMinDistance(distance * distance));
        }
        case "race" -> cond = joinAnd(cond, new ConditionTargetRace(Race.valueOf(a.getNodeValue())));
        case "using" -> {
          int mask = 0;
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            for (WeaponType wt : WeaponType.values()) {
              if (wt.name().equals(item)) {
                mask |= wt.mask();
                break;
              }
            }
            for (ArmorType at : ArmorType.values()) {
              if (at.name().equals(item)) {
                mask |= at.mask();
                break;
              }
            }
          }
          cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
        }
        case "npcid" -> {
          StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
          List<Integer> array = new ArrayList<>(st.countTokens());
          while (st.hasMoreTokens()) {
            String item = st.nextToken().trim();
            array.add(Integer.decode(getValue(item, null)));
          }
          cond = joinAnd(cond, new ConditionTargetNpcId(array));
        }
        case "npctype" -> {
          String values = getValue(a.getNodeValue(), template).trim();
          String[] valuesSplit = values.split(",");
          InstanceType[] types = new InstanceType[valuesSplit.length];
          for (int j = 0; j < valuesSplit.length; j++) {
            types[j] = Enum.valueOf(InstanceType.class, valuesSplit[j]);
          }
          cond = joinAnd(cond, new ConditionTargetNpcType(types));
        }
        case "weight" -> {
          int weight = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionTargetWeight(weight));
        }
        case "invsize" -> {
          int size = Integer.decode(getValue(a.getNodeValue(), null));
          cond = joinAnd(cond, new ConditionTargetInvSize(size));
        }
        case "checkabnormal" -> {
          final String value = a.getNodeValue();
          if (value.contains(";")) {
            final String[] values = value.split(";");
            final var type = AbnormalType.valueOf(values[0]);
            final var level = Integer.decode(getValue(values[1], template));
            final var mustHave = Boolean.parseBoolean(values[2]);
            cond = joinAnd(cond, new ConditionCheckAbnormal(type, level, mustHave));
          } else {
            final var level = Integer.decode(getValue(value, template));
            cond = joinAnd(cond, new ConditionCheckAbnormal(AbnormalType.valueOf(value), level, true));
          }
          break;
        }
      }
    }

    if (cond == null) {
      LOG.error("Unrecognized <target> condition in " + file);
    }
    return cond;
  }
  protected void parseTemplate(Node n, Object template) {
    parseTemplate(n, template, null);
  }

  protected void parseTemplate(Node n, Object template, EffectScope effectScope) {
    Condition condition = null;
    n = n.getFirstChild();
    if (n == null) {
      return;
    }
    if ("cond".equalsIgnoreCase(n.getNodeName())) {
      condition = parseCondition(n.getFirstChild(), template);
      Node msg = n.getAttributes().getNamedItem("msg");
      Node msgId = n.getAttributes().getNamedItem("msgId");
      if ((condition != null) && (msg != null)) {
        condition.setMessage(msg.getNodeValue());
      } else if ((condition != null) && (msgId != null)) {
        condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
        Node addName = n.getAttributes().getNamedItem("addName");
        if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
          condition.addName();
        }
      }
      n = n.getNextSibling();
    }
    for (; n != null; n = n.getNextSibling()) {
      final String name = n.getNodeName().toLowerCase();

      switch (name) {
        case "effect" -> {
          if (template instanceof AbstractEffect) {
            throw new RuntimeException("Nested effects");
          }
          attachEffect(n, template, condition, effectScope);
        }
        case "add", "sub", "mul", "div", "set", "share", "enchant", "enchanthp" -> attachFunc(n, template, name, condition);
      }
    }
  }

  protected void attachFunc(Node n, Object template, String functionName, Condition attachCond) {
    Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
    int order = -1;
    final Node orderNode = n.getAttributes().getNamedItem("order");
    if (orderNode != null) {
      order = Integer.parseInt(orderNode.getNodeValue());
    }

    String valueString = n.getAttributes().getNamedItem("val").getNodeValue();
    double value;
    if (valueString.charAt(0) == '#') {
      value = Double.parseDouble(getTableValue(valueString));
    } else {
      value = Double.parseDouble(valueString);
    }

    final Condition applyCond = parseCondition(n.getFirstChild(), template);
    final FuncTemplate ft = new FuncTemplate(attachCond, applyCond, functionName, order, stat, value);
    if (template instanceof L2Item) {
      ((L2Item) template).attach(ft);
    } else if (template instanceof AbstractEffect) {
      ((AbstractEffect) template).attach(ft);
    } else {
      throw new RuntimeException("Attaching stat to a non-effect template!!!");
    }
  }

  protected String getValue(String value, Object template) {
    // is it a table?
    if (value.charAt(0) == '#') {
      if (template instanceof Skill) {
        return getTableValue(value);
      } else if (template instanceof Integer) {
        return getTableValue(value, (Integer) template);
      } else {
        throw new IllegalStateException();
      }
    }
    return value;
  }

  protected void setExtractableSkillData(StatsSet set, String value) {
    set.set("capsuled_items_skill", value);
  }

  protected void attachEffect(Node n, Object template, Condition attachCond, EffectScope effectScope) {
    final NamedNodeMap attrs = n.getAttributes();
    final StatsSet set = new StatsSet();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node att = attrs.item(i);
      set.set(att.getNodeName(), getValue(att.getNodeValue(), template));
    }

    final StatsSet parameters = parseParameters(n.getFirstChild(), template);
    final Condition applyCond = parseCondition(n.getFirstChild(), template);

    if (template instanceof Identifiable) {
      set.set("id", ((Identifiable) template).getId());
    }

    final AbstractEffect effect = effectHandler.createEffect(attachCond, applyCond, set, parameters);
    parseTemplate(n, effect);
    if (template instanceof L2Item) {
      LOG.error("Item " + template + " with effects!!!");
    } else if (template instanceof Skill) {
      final Skill skill = (Skill) template;
      if (effectScope != null) {
        skill.addEffect(effectScope, effect);
      } else if (skill.isPassive()) {
        skill.addEffect(EffectScope.PASSIVE, effect);
      } else {
        skill.addEffect(EffectScope.GENERAL, effect);
      }
    }
  }

  private StatsSet parseParameters(Node n, Object template) {
    StatsSet parameters = null;
    while ((n != null)) {
      // Parse all parameters.
      if ((n.getNodeType() == Node.ELEMENT_NODE) && "param".equals(n.getNodeName())) {
        if (parameters == null) {
          parameters = new StatsSet();
        }
        NamedNodeMap params = n.getAttributes();
        for (int i = 0; i < params.getLength(); i++) {
          Node att = params.item(i);
          parameters.set(att.getNodeName(), getValue(att.getNodeValue(), template));
        }
      }
      n = n.getNextSibling();
    }
    return parameters == null ? StatsSet.EMPTY_STATSET : parameters;
  }

  private void makeSkills() {
    int count = 0;
    currentSkill.currentSkills =
            new ArrayList<>(
                    currentSkill.sets.length
                            + currentSkill.enchsets1.length
                            + currentSkill.enchsets2.length
                            + currentSkill.enchsets3.length
                            + currentSkill.enchsets4.length
                            + currentSkill.enchsets5.length
                            + currentSkill.enchsets6.length
                            + currentSkill.enchsets7.length
                            + currentSkill.enchsets8.length);
    StatsSet set;
    for (int i = 0; i < currentSkill.sets.length; i++) {
      set = currentSkill.sets[i];
      try {
        currentSkill.currentSkills.add(i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    int _count = count;
    for (int i = 0; i < currentSkill.enchsets1.length; i++) {
      set = currentSkill.enchsets1[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets2.length; i++) {
      set = currentSkill.enchsets2[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets3.length; i++) {
      set = currentSkill.enchsets3[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets4.length; i++) {
      set = currentSkill.enchsets4[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets5.length; i++) {
      set = currentSkill.enchsets5[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets6.length; i++) {
      set = currentSkill.enchsets6[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets7.length; i++) {
      set = currentSkill.enchsets7[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
    _count = count;
    for (int i = 0; i < currentSkill.enchsets8.length; i++) {
      set = currentSkill.enchsets8[i];
      try {
        currentSkill.currentSkills.add(_count + i, new Skill(set));
        count++;
      } catch (Exception ex) {
        LOG.error(
                "Skill id=" + set.getInt("skill_id") + " level=" + set.getInt("level"),
                ex);
      }
    }
  }

  public static class SkillInfo {
    public int id;
    public String name;
    public StatsSet[] sets;
    public StatsSet[] enchsets1;
    public StatsSet[] enchsets2;
    public StatsSet[] enchsets3;
    public StatsSet[] enchsets4;
    public StatsSet[] enchsets5;
    public StatsSet[] enchsets6;
    public StatsSet[] enchsets7;
    public StatsSet[] enchsets8;
    public int currentLevel;
    public List<Skill> skills = new ArrayList<>();
    public List<Skill> currentSkills = new ArrayList<>();
  }

  private static class SingletonHolder {
    protected static final SkillData _instance = new SkillData(null);
  }
}
