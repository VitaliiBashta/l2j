package com.l2jserver.gameserver.dao.impl.mysql;

import static com.l2jserver.gameserver.config.Configuration.general;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.SkillDAO;
import com.l2jserver.gameserver.data.xml.impl.ClassListData;
import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.IllegalActionPunishmentType;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;


public class SkillDAOMySQLImpl implements SkillDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(SkillDAOMySQLImpl.class);
	
	private static final String SELECT = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
	
	private static final String INSERT = "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	
	private static final String UPDATE = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
	
	private static final String REPLACE = "REPLACE INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	
	private static final String DELETE_ONE = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	
	private static final String DELETE_ALL = "DELETE FROM character_skills WHERE charId=? AND class_index=?";

	public SkillDAOMySQLImpl() {
	}

	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, player.getClassIndex());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					final int id = rs.getInt("skill_id");
					final int level = rs.getInt("skill_level");
					
					// Create a L2Skill object for each record
					final Skill skill = SkillData.getInstance().getSkill(id, level);
					
					if (skill == null) {
						LOG.warn("Skipped null skill Id: {}, Level: {} while restoring player skills for {}", id, level, this);
						continue;
					}
					
					// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
					player.addSkill(skill);
					
					if (general().skillCheckEnable() && (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || general().skillCheckGM())) {
						if (!SkillTreesData.getInstance().isSkillAllowed(player, skill)) {
							Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has invalid skill " + skill.getName() + " (" + skill.getId() + "/" + skill.getLevel() + "), class:"
								+ ClassListData.getInstance().getClass(player.getClassId()).getClassName(), IllegalActionPunishmentType.BROADCAST);
							if (general().skillCheckRemove()) {
								player.removeSkill(skill);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			LOG.error("Could not restore {} skills!", player, ex);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, int classIndex, Skill skill) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, skill.getId());
			ps.setInt(3, skill.getLevel());
			ps.setInt(4, classIndex);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Error could not store char skills!", ex);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, int newClassIndex, List<Skill> newSkills) {
		if (newSkills.isEmpty()) {
			return;
		}
		
		final int classIndex = (newClassIndex > -1) ? newClassIndex : player.getClassIndex();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(REPLACE)) {
			con.setAutoCommit(false);
			for (final Skill addSkill : newSkills) {
				
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, addSkill.getId());
				ps.setInt(3, addSkill.getLevel());
				ps.setInt(4, classIndex);
				ps.addBatch();
			}
			ps.executeBatch();
			con.commit();
		} catch (Exception ex) {
			LOG.error("Error could not store {} skills!", player, ex);
		}
	}
	
	@Override
	public void update(L2PcInstance player, int classIndex, Skill newSkill, Skill oldSkill) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE)) {
			ps.setInt(1, newSkill.getLevel());
			ps.setInt(2, oldSkill.getId());
			ps.setInt(3, player.getObjectId());
			ps.setInt(4, classIndex);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Error could not store char skills!", ex);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, Skill skill) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE_ONE)) {
			ps.setInt(1, skill.getId());
			ps.setInt(2, player.getObjectId());
			ps.setInt(3, player.getClassIndex());
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Error could not delete skill!", ex);
		}
	}
	
	@Override
	public void deleteAll(L2PcInstance player, int classIndex) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE_ALL)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Error could not delete skill!", ex);
		}
	}
}
