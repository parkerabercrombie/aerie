import { GraphQLClient } from 'graphql-request';
import {
  simulatedActivitiesBatchLoader,
  simulatedActivityInstanceBySimulatedActivityIdBatchLoader,
} from '../../src/lib/batchLoaders/simulatedActivityBatchLoader.js';
import { removeMissionModel, uploadMissionModel } from '../utils/MissionModel.js';
import { createPlan, removePlan } from '../utils/Plan';
import { convertActivityIdToSimulatedActivityId, insertActivity, removeActivity } from '../utils/Activity';
import { executeSimulation, removeSimulationArtifacts } from '../utils/Simulation';
import DataLoader from 'dataloader';
import { activitySchemaBatchLoader } from '../../src/lib/batchLoaders/activitySchemaBatchLoader.js';

let graphqlClient: GraphQLClient;
let missionModelId: number;
let planId: number;
let activityId: number;
let simulationArtifactIds: { simulationId: number; simulationDatasetId: number };

beforeAll(async () => {
  graphqlClient = new GraphQLClient(process.env.MERLIN_GRAPHQL_URL as string);
  missionModelId = await uploadMissionModel(graphqlClient);
  planId = await createPlan(graphqlClient, missionModelId);
  activityId = await insertActivity(graphqlClient, planId, 'ParameterTest');
  simulationArtifactIds = await executeSimulation(graphqlClient, planId);
});

afterAll(async () => {
  await removeSimulationArtifacts(graphqlClient, simulationArtifactIds);
  await removeActivity(graphqlClient, activityId);
  await removePlan(graphqlClient, planId);
  await removeMissionModel(graphqlClient, missionModelId);
});

it('should load simulated activity instances for simulation_dataset', async () => {
  const activitySchemaDataLoader = new DataLoader(activitySchemaBatchLoader({ graphqlClient }));
  const simulatedActivities = await simulatedActivitiesBatchLoader({ graphqlClient, activitySchemaDataLoader })([
    { simulationDatasetId: simulationArtifactIds.simulationDatasetId },
  ]);
  if (simulatedActivities[0] instanceof Error) {
    throw simulatedActivities[0];
  }
  expect(simulatedActivities[0][0].activityTypeName).toBe('ParameterTest');
});

it('should load simulated activity instance for simulation_dataset and simulated activity id', async () => {
  const activitySchemaDataLoader = new DataLoader(activitySchemaBatchLoader({ graphqlClient }));

  const simulatedActivityId = await convertActivityIdToSimulatedActivityId(
    graphqlClient,
    simulationArtifactIds.simulationDatasetId,
    activityId,
  );

  const activityInstances = await simulatedActivityInstanceBySimulatedActivityIdBatchLoader({
    graphqlClient,
    activitySchemaDataLoader,
  })([{ simulationDatasetId: simulationArtifactIds.simulationDatasetId, simulatedActivityId }]);
  if (activityInstances[0] instanceof Error) {
    throw activityInstances[0];
  }
  expect(activityInstances[0].activityTypeName).toBe('ParameterTest');
});
