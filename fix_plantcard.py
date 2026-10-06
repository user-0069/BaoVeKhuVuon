import re

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'r') as f:
    content = f.read()

# Add ID 8, 9, 10
replacement = """            else if (id==6) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_cherrybomb.png"); price = "150"; }
            else if (id==7) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_chomper.png"); price = "150"; }
            else if (id==8) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_peashooter.png"); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==9) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sniperplant.png"); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==10) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_snowpea.png"); price = String.valueOf(Constants.COST_SNOWPEA); }"""

content = re.sub(r'else if \(id==6\).*?else if \(id==7\)[^\n]*', replacement, content, flags=re.DOTALL)

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'w') as f:
    f.write(content)
