<template>
    <div class="bg-top">
        <div class="panel">
            <GeneralInfo @sendInfo="saveInfo" :characterIn="character"></GeneralInfo>
        </div>
        <div class="panel">
            <Skills @sendSkills="saveSkills" :skillsSet="character.paramMap.skills"
                :proficiency_bonusSet="calculateProficiencyBonus(character.paramMap.experience)"
                :abilitySet="character.paramMap.abillityScores" :editable="editable"></Skills>
        </div>
        <div class="panel">
            <Health @sendHealth="saveHealth" :characterIn="character.paramMap" ></Health>
        </div>
        <div class="panel">
            <Attacks @sendInventory="saveAttacks" :paramMap="character.paramMap"></Attacks>
        </div>
        <div class="panel">
            <Inventory @sendInventory="saveInventory" :inventorySet="character.paramMap.inventory1">
            </Inventory>
        </div>
        <div class="panel">
            <AdditionalInfo @sendInventory="sendAdditionalInfo" :paramMap="character.paramMap">
            </AdditionalInfo>
        </div>

    </div>
</template>


<script>
import Inventory from './Inventory.vue'
import Skills from './Skills.vue'
import GeneralInfo from './GeneralInfo.vue'
import Health from './Health.vue'
import AdditionalInfo from './AdditionalInfo.vue'
import Attacks from './Attacks.vue'

export default {
    name: 'CharacterSheet',
    data() {
        return {
            character: {
                name: '',
                paramMap: {
                    skills: {},
                    inventory1: [],
                    abillityScores: {
                        "Strength": 0,
                        "Dexterity": 0,
                        "Constitution": 0,
                        "Intelligence": 0,
                        "Wisdom": 0,
                        "Charisma": 0
                    },
                    abilitiesProficiency: {
                        "Strength": false,
                        "Dexterity": false,
                        "Constitution": false,
                        "Intelligence": false,
                        "Wisdom": false,
                        "Charisma": false
                    }
                },
                data: {
                    level: 0,
                    race: '',
                    class: ''
                }
            },
            levels: [
                0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000, 85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
            ],
            timer: null,
            timer2: null,
            counter: 0,
            timestamp: 0,
            editable: false,
            portraitId: 1,
            portrait: ''
        }
    },
    components: {
        Inventory,
        Skills,
        GeneralInfo,
        Health,
        AdditionalInfo,
        Attacks
    }, methods: {
        getCharacter() {
            if (this.counter <= 0) {
                this.token = localStorage.getItem('token')
                this.url = window.location.href.split(':8080')[0]
                console.log(this.url)
                if (this.$route.params.id != undefined) {
                    fetch(this.url + `:1290/characters/check-update/${this.$route.params.id}?timestamp=0`, {
                        method: 'GET',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + this.token
                        }
                    }
                    )
                        .then(response => {
                            console.log("Test2")
                            if (response.status == 200) {
                                return response.json();
                            } else {
                                return null
                            }

                        })
                        .then(data => {
                            console.log(data)
                            if (data !== null) {
                                this.character = data.object
                                console.log("T")
                                if (Object.keys(this.character.paramMap.skills).length == 0) {
                                    this.character.paramMap.skills = {
                                        "Acrobatics": false,
                                        "Animal Handling": false,
                                        "Arcana": false,
                                        "Athletics": false,
                                        "Deception": false,
                                        "History": false,
                                        "Insight": false,
                                        "Intimidation": false,
                                        "Investigation": false,
                                        "Medicine": false,
                                        "Nature": false,
                                        "Perception": false,
                                        "Performance": false,
                                        "Persuasion": false,
                                        "Religion": false,
                                        "Sleight of Hand": false,
                                        "Stealth": false,
                                        "Survival": false
                                    }
                                }
                                if (localStorage.getItem("username") == "admin") {
                                    this.editable = true
                                }
                                else if (this.character.owners.length > 0) {
                                    for (let i = 0; i < this.character.owners.length; i++) {
                                        this.editable = false
                                        if (this.character.owners[i].id == localStorage.getItem('id')) {
                                            this.editable = true
                                            break;
                                        }
                                    }
                                }


                                var Title = document.getElementsByTagName('title')[0];
                                Title.innerHTML = this.character.name + " - DnD Character Sheet";
                                this.portraitId = this.character.portraitId
                                this.timestamp = data.timestamp
                            }
                        }).then(() => {
                            this.$forceUpdate()
                        })

                }
            }
        },
        checkForUpdate() {
            this.token = localStorage.getItem('token')
            this.url = window.location.href.split(':8080')[0]
            console.log("ckeck with:" + this.timestamp)
            if (this.$route.params.id != undefined) {
                fetch(this.url + `:1290/characters/check-update/${this.$route.params.id}?timestamp=${this.timestamp}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + this.token
                    }
                })
                    .then(response => response.json())
                    .then(data => {
                        console.log(data.timestamp - this.timestamp)
                        if (this.timestamp != data.timestamp) {
                            console.log('update')
                            this.getCharacter()
                        }
                    })
            }

        },
        sendChanges() {
            this.token = localStorage.getItem('token')
            this.url = window.location.href.split(':8080')[0]
            fetch(this.url + `:1290/characters/${this.$route.params.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.token
                },
                body: JSON.stringify(this.character)
            }
            )
        },
        saveSkills(skills) {
            this.character.paramMap.skills = skills
            //this.sendChanges()
            this.counter = 10
        },
        saveInventory() {
            //this.sendChanges()
            this.counter = 10
        },
        saveInfo(NewCharacter) {
            this.character.name = NewCharacter.name
            this.character.paramMap.race = NewCharacter.race
            this.character.paramMap.class = NewCharacter.class
            this.character.paramMap.background = NewCharacter.background
            this.character.paramMap.alignment = NewCharacter.alignment
            this.character.paramMap.experience = NewCharacter.xp
            this.character.paramMap.abillityScores = NewCharacter.abilities
            this.character.paramMap.abilitiesProficiency = NewCharacter.abilitiesProficiency
            //this.sendChanges()
            this.counter = 10
        }, saveHealth(health) {
            this.character.paramMap.hp = health.hp
            this.character.paramMap.maxhp = health.maxhp
            this.character.paramMap.bonusHp = health.bonusHp
            this.character.paramMap.hitDice = health.hitDice
            this.character.paramMap.deathSavesSuccess = health.deathSavesSuccess
            this.character.paramMap.deathSavesFail = health.deathSavesFail
            this.character.paramMap.initiative = health.initiative
            this.character.paramMap.speed = health.speed
            this.character.paramMap.inspiration = health.inspiration
            this.character.paramMap.coins = health.coins
            this.character.paramMap.armorClass = health.armorClass
            this.character.paramMap.additionalSkills = health.additionalSkills
            //this.sendChanges()
            this.counter = 10
        }, sendAdditionalInfo(out) {
            this.character.paramMap.Traits = out
            //this.sendChanges()
            this.counter = 10
        },
        saveAttacks(out) {
            this.character.paramMap.Attacks = out
            //this.sendChanges()
            this.counter = 10

        },
        calculateProficiencyBonus(ex) {
            for (let i = 0; i < this.levels.length; i++) {
                if (this.character.paramMap.experience < this.levels[i]) {
                    this.level = i;
                    this.character.level = i;
                    this.proficiencyBonus = 2 + Math.floor((i - 1) / 4);
                    break;
                }
            }
            if (this.character.xp >= 355000) {
                this.level = 20;
                this.character.level = 20;
                this.proficiencyBonus = 6;
            }
            return this.proficiencyBonus;
        }, update() {
            this.checkForUpdate()
        }, update2() {
            this.counter--;
            if (this.counter == 0) {
                this.levelCalulate()
                this.character.data.level = this.level
                this.character.data.class = this.character.paramMap.class
                this.character.data.race = this.character.paramMap.race
                this.sendChanges()
            }
        },
        downloadImage(id) {
            this.url = window.location.href.split(':8080')[0]
            this.token = localStorage.getItem('token')
            fetch(this.url + ":1290/portraits/" + id, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.token
                }
            }).then(res => res.json())
                .then(data => {
                    this.portrait = data.data
                    let favicon = document.querySelector('link[rel="icon"]')
                    favicon.href = this.portrait

                })
        },
        levelCalulate() {
            for (let i = 0; i < this.levels.length; i++) {
                if (this.character.xp < this.levels[i]) {
                    this.level = i;
                    this.character.level = i;
                    this.proficiencyBonus = 2 + Math.floor((i - 1) / 4);
                    break;
                }
            }
            if (this.character.xp >= 355000) {
                this.level = 20;
                this.character.level = 20;
                this.proficiencyBonus = 6;
            }

        }

    },
    mounted() {
        this.getCharacter()
        this.timer = setInterval(() => {
            this.update()
        }, 500)
        this.timer2 = setInterval(() => {
            this.update2()
        }, 25)
        this.downloadImage(1)

    },
    beforeDestroy() {
        clearInterval(this.timer)
        clearInterval(this.timer2)
    },
    watch: {
        portraitId: function (newId) {
            this.token = localStorage.getItem('token')
            if (this.token) {
                this.downloadImage(newId)
            }
        }
    }
}


</script>

<style scoped>
.bg-top {
    display: flex;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    width: 100%;
    height: 100%;
    flex-wrap: wrap;
}

.panel {
    width: 400px;
    height: 700px;
    margin: 15px;
    border-radius: 15px;
    background-color: #333;
    padding: auto;
    display: flex;
}

@media only screen and (max-width: 525px) {
    .panel {
        width: 90vw;
    }
}
</style>