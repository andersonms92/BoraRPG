package com.borarpg.domain.rules.mission

import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.domain.model.mission.QuestPoint

object MissionRules {
    fun validate(quest: QuestPoint) {
        require(
            quest.generated &&
                quest.goalSteps in 200..5000 &&
                quest.xpReward in 1..1000 &&
                quest.goldReward in 0..1000 &&
                quest.itemReward.isNotBlank()
        ) {
            "Missão inválida."
        }
    }

    fun reward(hero: Hero, progress: MissionProgress): Hero {
        check(progress.ready) { "Complete a meta de passos para resgatar." }
        return hero.copy(
            xp = hero.xp + progress.quest.xpReward,
            gold = hero.gold + progress.quest.goldReward,
        )
    }
}
