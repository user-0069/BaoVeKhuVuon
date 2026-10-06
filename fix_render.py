import re

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'r') as f:
    content = f.read()

# Remove drawing of btnDir
content = re.sub(r'// Direction Button.*?g.drawString\(dirText, btnDir.x \+ 10, btnDir.y \+ 25\);', '', content, flags=re.DOTALL)

# Add text L and R for specific IDs
draw_logic = """
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12)); 
                g.drawString(c.price, c.rect.x + 30, c.rect.y + 70);
                if (c.id == 2 || c.id == 3 || c.id == 5) {
                    g.setColor(Color.RED);
                    g.drawString("(R)", c.rect.x + 5, c.rect.y + 15);
                } else if (c.id == 8 || c.id == 9 || c.id == 10) {
                    g.setColor(Color.RED);
                    g.drawString("(L)", c.rect.x + 5, c.rect.y + 15);
                }
"""

content = re.sub(r'g.setColor\(Color.WHITE\); g.setFont\(new Font\("Arial", Font.BOLD, 12\)\);.*?g.drawString\(c.price, c.rect.x \+ 30, c.rect.y \+ 70\);', draw_logic.strip(), content, flags=re.DOTALL)


with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'w') as f:
    f.write(content)
