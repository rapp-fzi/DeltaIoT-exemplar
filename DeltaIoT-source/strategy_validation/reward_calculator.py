import statistics
import math


class RewardCalculator:
    pass

class SimexpRewardCalculator(RewardCalculator):
    def __init__(self, energy_normalizer, packet_loss_normalizer):
        self._energy_normalizer = energy_normalizer
        self._packet_loss_normalizer = packet_loss_normalizer

    def calc_reward(self, energy: float, packet_loss: float) -> float:
        normalized_energy_consumption = self._energy_normalizer.normalize(energy)
        normalized_packet_loss = self._packet_loss_normalizer.normalize(packet_loss)
        normalized_reward = normalized_packet_loss + normalized_energy_consumption
        return normalized_reward


class SimulatorRewardCalculator(RewardCalculator):
    def __init__(self, energy_normalizer, packet_loss_normalizer):
        self._energy_normalizer = energy_normalizer
        self._packet_loss_normalizer = packet_loss_normalizer

    def calc_reward(self, energy: float, packet_loss: float) -> float:
        normalized_energy_consumption = self._energy_normalizer.normalize(energy)
        normalized_packet_loss = self._packet_loss_normalizer.normalize(packet_loss)
        normalized_reward = (normalized_packet_loss + normalized_energy_consumption) / 2
        return normalized_reward


class AverageTotalRewardCalculator:
    def __init__(self, sample_reward_calculator: RewardCalculator):
        self._sample_reward_calculator = sample_reward_calculator

    def total_reward(self, runs) -> float:
        run_rewards = []
        for run in runs:
            run_reward = self._run_reward(run)
            run_rewards.append(run_reward)
        average_reward = statistics.mean(run_rewards)
        return average_reward

    def _run_reward(self, run) -> float:
        qas = run["quality_attributes"]
        energy_consumptions = qas["EnergyConsumption.props"]
        packet_losss = qas["PacketLoss.props"]
        normalized_samples = []
        for i, energy in enumerate(energy_consumptions):
            packet_loss = packet_losss[i]
            sample_reward = self._sample_reward_calculator.calc_reward(energy, packet_loss)
            normalized_samples.append(sample_reward)
        run_reward_sum = math.fsum(normalized_samples)
        return run_reward_sum
