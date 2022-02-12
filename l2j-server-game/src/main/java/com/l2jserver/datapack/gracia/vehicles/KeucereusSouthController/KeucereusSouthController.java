
package com.l2jserver.datapack.gracia.vehicles.KeucereusSouthController;

import com.l2jserver.datapack.gracia.vehicles.AirShipController;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.VehiclePathPoint;
import org.springframework.stereotype.Service;

@Service
public class KeucereusSouthController extends AirShipController {
	private static final int DOCK_ZONE = 50603;
	private static final int LOCATION = 100;
	private static final int CONTROLLER_ID = 32517;
	
	private static final VehiclePathPoint[] ARRIVAL = {
		new VehiclePathPoint(-185312, 246544, 2500),
		new VehiclePathPoint(-185312, 246544, 1336)
	};
	
	private static final VehiclePathPoint[] DEPART = {
		new VehiclePathPoint(-185312, 246544, 1700, 280, 2000),
		new VehiclePathPoint(-186900, 251699, 1700, 280, 2000)
	};
	
	private static final VehiclePathPoint[][] TELEPORTS = {
		{
			new VehiclePathPoint(-185312, 246544, 1700, 280, 2000),
			new VehiclePathPoint(-186900, 251699, 1700, 280, 2000),
			new VehiclePathPoint(-186373, 234000, 2500, 0, 0)
		},
		{
			new VehiclePathPoint(-185312, 246544, 1700, 280, 2000),
			new VehiclePathPoint(-186900, 251699, 1700, 280, 2000),
			new VehiclePathPoint(-206692, 220997, 3000, 0, 0)
		},
		{
			new VehiclePathPoint(-185312, 246544, 1700, 280, 2000),
			new VehiclePathPoint(-186900, 251699, 1700, 280, 2000),
			new VehiclePathPoint(-235693, 248843, 5100, 0, 0)
		}
	};
	
	private static final int[] FUEL = {
		0,
		50,
		100
	};
	
	public KeucereusSouthController() {
		super(-1, KeucereusSouthController.class.getSimpleName(), "gracia/vehicles");
		addStartNpc(CONTROLLER_ID);
		addFirstTalkId(CONTROLLER_ID);
		addTalkId(CONTROLLER_ID);
		
		_dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);
		
		_shipSpawnX = -184527;
		_shipSpawnY = 243611;
		_shipSpawnZ = 3000;
		
		_locationId = LOCATION;
		_arrivalPath = ARRIVAL;
		_departPath = DEPART;
		_teleportsTable = TELEPORTS;
		_fuelTable = FUEL;
		
		_oustLoc = new Location(-186148, 246296, 1360);
		
		_movieId = 1000;
		
		validityCheck();
	}
}