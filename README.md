# CoswayUtil
<img src="http://ycs.newt-tech.com/images/ntlogot.png">
a utility plugin for Newt-Tech servers, a yescraft network server
<hr>
<ul>
<li>Commands</li>
    <ul>
        <li><code>/scale [0.1-25] </code></li>
        <li><code>/throw</code></li>
        <li><code>/gravitygauntlet</code></li>
        <li><code>/getwand</code></li>
        <li><code>/launchstick</code></li>
        <li><code>/rapidbow</code></li>
<label>give shop book</label>
        <li><code>/shopbook</code></li>
<label>open block shop</label>
        <li><code>/blockshop</code></li>
<label>full repair item</label>
        <li><code>/repair</code></li>
<label>make item unbreakable</label>
        <li><code>/unbreakable</code></li>
<label>Enchant item</label>
        <li><code>/mage [enchant] [level] </code></li>
<label>reload configs</label>
        <li><code>/coswayreload</code></li>
    </ul>
</ul>

<h3>new revamped reward system include incrimentals for xp levels, skill level, enchants, capped damage, armor and more</h3>


<h2>Config.yml</h2>
```yaml

shop:
  categories:
    stone_blocks:
      items:
        cobble:
          material: COBBLESTONE
          price: 64
          quantity: 64
          display_name: "Cobblestone Block"
          available: true
        smooth_stone:
          material: SMOOTH_STONE
          price: 320
          quantity: 64
          display_name: "Smooth Stone"
          available: true
        dirt:
          material: DIRT
          price: 64
          quantity: 64
          display_name: "Dort"
          available: true
    transparents:
      items:
        glass:
          material: GLASS
          price: 384
          quantity: 64
          display_name: "Glass"
          available: true
    wood_blocks:
      items:
        oak_planks:
          material: OAK_PLANKS
          price: 960
          quantity: 64
          display_name: "Oak Planks"
          available: true
        oak_log:
          material: OAK_LOG
          price: 3840
          quantity: 64
          display_name: "Oak Log"
          available: true
    ore:
      items:
        slime:
          material: SLIME_BALL
          price: 200
          quantity: 32
          display_name: "Green Balls"
          available: true
        gunpowder:
          material: GUNPOWDER
          price: 50
          quantity: 32
          display_name: “Boom Dust”
          available: true
        netherite:
          material: NETHERITE_INGOT
          price: 1450
          quantity: 1
          display_name: "Netherite Ingot"
          available: true
        iron:
          material: IRON_INGOT
          price: 22
          quantity: 1
          display_name: "Iron Ingot"
          available: true
        gold:
          material: GOLD_INGOT
          price: 105
          quantity: 1
          display_name: "Gold Ingot"
          available: true
        copper:
          material: COPPER_INGOT
          price: 15
          quantity: 1
          display_name: "Copper Ingot"
          available: true
        diamond:
          material: DIAMOND
          price: 200
          quantity: 1
          display_name: "DIMINS!"
          available: true
        emerald:
          material: EMERALD
          price: 1000
          quantity: 1
          display_name: "EZMERALDAS"
          available: true
        lapiz:
          material: LAPIS_LAZULI
          price: 350
          quantity: 5
          display_name: "Enchantment Cookies"
          available: true
    pvp:
      items:
        golden_apple:
          material: GOLDEN_APPLE
          price: 30000
          quantity: 2
          display_name: "Golden Apple"
          available: true
        egolden_apple:
          material: ENCHANTED_GOLDEN_APPLE
          price: 6000000
          quantity: 2
          display_name: "Gapple"
          available: true
        iron_helmet:
          material: IRON_HELMET
          price: 110
          quantity: 1
          display_name: "Iron Helmet"
          available: true
        iron_chestplate:
          material: IRON_CHESTPLATE
          price: 176
          quantity: 1
          display_name: "Iron Chestplate"
          available: true
        iron_leggings:
          material: IRON_LEGGINGS
          price: 154
          quantity: 1
          display_name: "Iron Leggings"
          available: true
        iron_boots:
          material: IRON_BOOTS
          price: 88
          quantity: 1
          display_name: "Iron Boots"
          available: true
        iron_sword:
          material: IRON_SWORD
          price: 44
          quantity: 1
          display_name: "Iron Sword"
          available: true
        iron_PICKAXE:
          material: IRON_PICKAXE
          price: 66
          quantity: 1
          display_name: "Iron Pick"
          available: true
        iron_axe:
          material: IRON_AXE
          price: 66
          quantity: 1
          display_name: "Iron Axe"
          available: true
        iron_shovel:
          material: IRON_SHOVEL
          price: 22
          quantity: 1
          display_name: "Iron Spoon"
          available: true
    redstone:
      items:
        repeater:
          material: REPEATER
          price: 900
          quantity: 18
          display_name: "Repeater"
          available: true
        comparator:
          material: comparator
          price: 1500
          quantity: 5
          display_name: "Comparator"
          available: true
        redstone:
          material: REDSTONE
          price: 2048
          quantity: 64
          display_name: "Redstone Dust"
          available: true
        slime:
          material: SLIME_BLOCK
          price: 500
          quantity: 2
          display_name: "Slime Block"
          available: true
    nether:
      items:
        obsidian:
          material: OBSIDIAN
          price: 1300
          quantity: 10
          display_name: "Obsidian"
          available: true
        upgrade:
          material: NETHERITE_UPGRADE_SMITHING_TEMPLATE
          price: 900000
          quantity: 1
          display_name: "Netherite Upgrade"
          available: true
        glowstone:
          material: GLOWSTONE
          price: 400
          quantity: 4
          display_name: "GlowStone"
          available: true
        crystal:
          material: END_CRYSTAL
          price: 800000000
          quantity: 4
          display_name: "boom orbs"
          available: true
        shells:
          material: SHULKER_SHELL
          price: 1600
          quantity: 2
          display_name: "Shulker Shell"
          available: true
    end:
      items:
        elytra:
          material: ELYTRA
          price: 1900000000
          quantity: 1
          display_name: "Wangs"
          available: true
    utils:
      items:
        ask:
          material: KNOWLEDGE_BOOK
          price: 400000
          quantity: 1
          display_name: "Anchor Shield Kit"
          available: true
          tool_id: 1
    utils1:
      items:
        gravity_gauntlet:
          material: KNOWLEDGE_BOOK
          price: 1500000
          quantity: 1
          display_name: "Gravity Gauntlet"
          available: true
          tool_id: 2
    utils2:
      items:
        levitation_wand:
          material: KNOWLEDGE_BOOK
          price: 600000
          quantity: 1
          display_name: "Levitation Wand"
          available: true
          tool_id: 3
    utils3:
      items:
        launch_stick:
          material: KNOWLEDGE_BOOK
          price: 2000000
          quantity: 1
          display_name: "Launch Stick"
          available: true
          tool_id: 4
    utils4:
      items:
        rapid_fire_bow:
          material: KNOWLEDGE_BOOK
          price: 1000000
          quantity: 1
          display_name: "Rapid Fire Bow"
          available: true
          tool_id: 5
    utils5:
      items:
        illumination_wand:
          material: KNOWLEDGE_BOOK
          price: 500000
          quantity: 1
          display_name: "Illumination Wand"
          available: true
          tool_id: 6
    utils6:
      items:
        maceofstorms:
          material: KNOWLEDGE_BOOK
          price: 600000
          quantity: 1
          display_name: "Mace Of Storms"
          available: true
          tool_id: 7
    utils7:
      items:
        tracerbow:
          material: KNOWLEDGE_BOOK
          price: 54000
          quantity: 1
          display_name: "Tracer Bow"
          available: true
          tool_id: 8
    utils8:
      items:
        wither_skull_wand:
          material: KNOWLEDGE_BOOK
          price: 1500000
          quantity: 1
          display_name: "Wither Skull Cannon"
          available: true
          tool_id: 9
    utils9:
      items:
        missile_wand:
          material: KNOWLEDGE_BOOK
          price: 1230000
          quantity: 1
          display_name: "Seeking Missile Launcher"
          available: true
          tool_id: 10

treasure:
  - DIAMOND
  - EMERALD
  - GOLD_INGOT
  - NETHERITE_SCRAP
  - ENCHANTED_GOLDEN_APPLE
  - TOTEM_OF_UNDYING

poor: "you're too poor for that, get gooder..."
inventory_full: "you got too much clutter, cant fit your purchase into that mess..."
pagination:
  max_items_per_page: 45  # Max items to display per page
economy-rewards:

  enabled: true

  base:
    reward-per-heart: 2.5

  messages:
    enabled: true
    buffer-time: 40
    reward: "&aRewarded $%amount% for killing &e%count% %entity%'s"

  modifiers:

    damage:
      enabled: true
      percent-per-heart: 2.0

    experience:
      enabled: true
      percent-per-level: 1.0

    critical-hit:
      enabled: true
      percent: 10

    sprint-hit:
      enabled: true
      percent: 5

    projectile:
      enabled: true
      percent: 5

    fire-aspect:
      enabled: true
      percent: 2

    looting:
      enabled: true
      percent-per-level: 2

    baby-mobs:
      enabled: true
      percent: 15

    named-mobs:
      enabled: true
      percent: 20

    night:
      enabled: true
      percent: 5

    rain:
      enabled: true
      percent: 3

  blacklist:
    - ARMOR_STAND
    - ITEM_FRAME
wither-skull-launcher:
  explosion-power: 50.0
heat-seeking-missile:

  targets:
    enabled: true
    entities:
      - ZOMBIE
      - SKELETON
      - CREEPER
      - SPIDER
      - ENDERMAN
      - WARDEN
      - PLAYER
      - PIG
      - HORSE
      - VILLAGER
      - CHICKEN
      - BAT
      - WITCH
      - COW
      - SHEEP

  tracking:
    range: 30
    max-life-ticks: 600
    speed: 1.7
    steering-strength: 0.45
    # How many ticks before forcing a new target search
    retarget-delay: 20
    # How many ticks before assuming missile is stuck
    stuck-check-delay: 10
    # Velocity boost when stuck
    recovery-boost: 1.2
    water-recovery-delay: 20
    water-recovery-boost: 3.0


  explosion:
    entity-damage: 20

    # Enables actual explosion damage and terrain destruction
    fiery-explosion:
      enabled: false

    power: 8

    # Creates a rainbow firework visual burst
    rainbow-firework:
      enabled: true
```