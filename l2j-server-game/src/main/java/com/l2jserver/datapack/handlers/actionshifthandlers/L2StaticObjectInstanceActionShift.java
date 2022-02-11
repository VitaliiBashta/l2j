
package com.l2jserver.datapack.handlers.actionshifthandlers;

import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.handler.IActionShiftHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.StaticObject;
import com.l2jserver.gameserver.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class L2StaticObjectInstanceActionShift implements IActionShiftHandler {
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact) {
		if (activeChar.getAccessLevel().isGm()) {
			activeChar.setTarget(target);
			activeChar.sendPacket(new StaticObject((L2StaticObjectInstance) target));
			
			final NpcHtmlMessage html = new NpcHtmlMessage(StringUtil.concat("<html><body><center><font color=\"LEVEL\">Static Object Info</font></center><br><table border=0><tr><td>Coords X,Y,Z: </td><td>", String.valueOf(target.getX()), ", ", String.valueOf(target.getY()), ", ", String.valueOf(target.getZ()), "</td></tr><tr><td>Object ID: </td><td>", String.valueOf(target.getObjectId()), "</td></tr><tr><td>Static Object ID: </td><td>", String.valueOf(target.getId()), "</td></tr><tr><td>Mesh Index: </td><td>", String.valueOf(((L2StaticObjectInstance) target).getMeshIndex()), "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>", target.getClass().getSimpleName(), "</td></tr></table></body></html>"));
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2StaticObjectInstance;
	}
}
