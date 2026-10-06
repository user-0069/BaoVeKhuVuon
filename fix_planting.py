import re

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'r') as f:
    content = f.read()

cost_logic = """
            int cost = 0;
            if (selectedPlant == 1) cost = Constants.COST_SUNFLOWER; 
            else if (selectedPlant == 2 || selectedPlant == 8) cost = Constants.COST_PEASHOOTER;
            else if (selectedPlant == 3 || selectedPlant == 9) cost = Constants.COST_SNIPER; 
            else if (selectedPlant == 4) cost = Constants.COST_WALLNUT;
            else if (selectedPlant == 5 || selectedPlant == 10) cost = Constants.COST_SNOWPEA;
            else if (selectedPlant == 6) cost = 150;
            else if (selectedPlant == 7) cost = 150;
"""

plant_logic = """
                Plant p = null;
                if (selectedPlant == 1) p = new Sunflower(col, row);
                else if (selectedPlant == 2) { p = new Peashooter(col, row); p.direction = 1; }
                else if (selectedPlant == 8) { p = new Peashooter(col, row); p.direction = 3; }
                else if (selectedPlant == 3) { p = new SniperPlant(col, row); p.direction = 1; }
                else if (selectedPlant == 9) { p = new SniperPlant(col, row); p.direction = 3; }
                else if (selectedPlant == 4) p = new WallNut(col, row);
                else if (selectedPlant == 5) { p = new SnowPea(col, row); p.direction = 1; }
                else if (selectedPlant == 10) { p = new SnowPea(col, row); p.direction = 3; }
                else if (selectedPlant == 6) p = new com.team.gardendefense.model.entity.plant.CherryBomb(col, row, this);
                else if (selectedPlant == 7) p = new com.team.gardendefense.model.entity.plant.Chomper(col, row, this);
"""

content = re.sub(r'int cost = 0;.*?else if \(selectedPlant == 7\) cost = 150;', cost_logic.strip(), content, flags=re.DOTALL)
content = re.sub(r'Plant p = null;.*?else if \(selectedPlant == 7\) p = new com.team.gardendefense.model.entity.plant.Chomper\(col, row, this\);', plant_logic.strip(), content, flags=re.DOTALL)

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'w') as f:
    f.write(content)
