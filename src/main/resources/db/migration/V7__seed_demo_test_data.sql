-- V7: Seed realistic test incidents for demo users
-- Incidents created by Alice (USER), assigned to Bob/David (AGENT), managed by Carol (MANAGER)

DO $$
DECLARE
  alice_id   UUID;
  bob_id     UUID;
  david_id   UUID;
  carol_id   UUID;
  admin_id   UUID;
  seq_start  BIGINT;

  i1 UUID := gen_random_uuid();
  i2 UUID := gen_random_uuid();
  i3 UUID := gen_random_uuid();
  i4 UUID := gen_random_uuid();
  i5 UUID := gen_random_uuid();
  i6 UUID := gen_random_uuid();
  i7 UUID := gen_random_uuid();
  i8 UUID := gen_random_uuid();

BEGIN
  SELECT id INTO alice_id FROM users WHERE email = 'alice@tenant.io'   LIMIT 1;
  SELECT id INTO bob_id   FROM users WHERE email = 'bob@property.io'   LIMIT 1;
  SELECT id INTO david_id FROM users WHERE email = 'david@property.io' LIMIT 1;
  SELECT id INTO carol_id FROM users WHERE email = 'carol@property.io' LIMIT 1;
  SELECT id INTO admin_id FROM users WHERE email = 'admin@resolvehub.io' LIMIT 1;

  -- Only seed if demo users exist and have no incidents yet
  IF alice_id IS NULL OR bob_id IS NULL THEN
    RETURN;
  END IF;

  IF EXISTS (SELECT 1 FROM incidents WHERE created_by = alice_id LIMIT 1) THEN
    RETURN;
  END IF;

  INSERT INTO incidents (id, title, description, category, priority, status, created_by, assigned_to, created_at, updated_at, case_number)
  VALUES
    (i1, 'Heating not working in Flat B204',
     'Radiators are cold in all rooms. Thermostat shows no response. Temperature has been dropping since last night.',
     'MAINTENANCE', 'HIGH', 'IN_PROGRESS',
     alice_id, bob_id,
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day',
     nextval('incident_case_number_seq')),

    (i2, 'Water leak under bathroom sink',
     'Constant dripping from pipe joint under the sink. Water pooling on floor tiles. Bucket fills within hours.',
     'MAINTENANCE', 'HIGH', 'ASSIGNED',
     alice_id, david_id,
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days',
     nextval('incident_case_number_seq')),

    (i3, 'Broken front door lock — security risk',
     'Key turns but deadbolt does not engage. Main entrance cannot be locked. Urgent safety concern.',
     'SAFETY', 'CRITICAL', 'IN_PROGRESS',
     alice_id, bob_id,
     NOW() - INTERVAL '4 days', NOW() - INTERVAL '12 hours',
     nextval('incident_case_number_seq')),

    (i4, 'Noise complaint — loud music after midnight',
     'Loud music from Flat C305 between 1am and 3am, three nights running. Other residents on floor 3 are also affected.',
     'NOISE', 'MEDIUM', 'NEW',
     alice_id, NULL,
     NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day',
     nextval('incident_case_number_seq')),

    (i5, 'Wi-Fi unavailable in Room C312',
     'No wireless signal detected since yesterday evening. Router indicator light is off. Multiple rooms on the same floor affected.',
     'INTERNET', 'MEDIUM', 'ASSIGNED',
     alice_id, david_id,
     NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days',
     nextval('incident_case_number_seq')),

    (i6, 'Deposit deduction dispute — carpet cleaning',
     'Charged £150 for carpet cleaning but carpet was professionally cleaned before checkout. Receipt available as evidence.',
     'DEPOSIT', 'MEDIUM', 'NEW',
     alice_id, NULL,
     NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours',
     nextval('incident_case_number_seq')),

    (i7, 'Mould near bedroom window',
     'Black mould patches spreading on wall beside the window frame. Getting worse each week. Affecting air quality in the room.',
     'MAINTENANCE', 'HIGH', 'RESOLVED',
     alice_id, bob_id,
     NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days',
     nextval('incident_case_number_seq')),

    (i8, 'Communal hallway — request for deep clean',
     'Ground floor hallway has not been properly cleaned for over two weeks. Visible dirt and scuff marks throughout.',
     'CLEANING', 'LOW', 'CLOSED',
     alice_id, david_id,
     NOW() - INTERVAL '14 days', NOW() - INTERVAL '7 days',
     nextval('incident_case_number_seq'));

  -- Seed audit logs for the incidents
  INSERT INTO audit_logs (id, incident_id, actor_id, action, old_value, new_value, message, created_at, updated_at)
  VALUES
    (gen_random_uuid(), i1, alice_id, 'INCIDENT_CREATED', NULL, NULL, 'Incident created', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (gen_random_uuid(), i1, carol_id, 'INCIDENT_ASSIGNED', 'Unassigned', 'Bob Torres', 'Assigned to Bob Torres', NOW() - INTERVAL '3 days' + INTERVAL '1 hour', NOW() - INTERVAL '3 days' + INTERVAL '1 hour'),
    (gen_random_uuid(), i1, bob_id, 'STATUS_CHANGED', 'ASSIGNED', 'IN_PROGRESS', 'Status changed from ASSIGNED to IN_PROGRESS', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    (gen_random_uuid(), i2, alice_id, 'INCIDENT_CREATED', NULL, NULL, 'Incident created', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), i2, carol_id, 'INCIDENT_ASSIGNED', 'Unassigned', 'David Kim', 'Assigned to David Kim', NOW() - INTERVAL '2 days' + INTERVAL '30 minutes', NOW() - INTERVAL '2 days' + INTERVAL '30 minutes'),

    (gen_random_uuid(), i3, alice_id, 'INCIDENT_CREATED', NULL, NULL, 'Incident created', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (gen_random_uuid(), i3, carol_id, 'INCIDENT_ASSIGNED', 'Unassigned', 'Bob Torres', 'Assigned to Bob Torres', NOW() - INTERVAL '4 days' + INTERVAL '2 hours', NOW() - INTERVAL '4 days' + INTERVAL '2 hours'),

    (gen_random_uuid(), i7, alice_id, 'INCIDENT_CREATED', NULL, NULL, 'Incident created', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (gen_random_uuid(), i7, carol_id, 'INCIDENT_ASSIGNED', 'Unassigned', 'Bob Torres', 'Assigned to Bob Torres', NOW() - INTERVAL '10 days' + INTERVAL '1 hour', NOW() - INTERVAL '10 days' + INTERVAL '1 hour'),
    (gen_random_uuid(), i7, bob_id, 'STATUS_CHANGED', 'ASSIGNED', 'RESOLVED', 'Status changed from IN_PROGRESS to RESOLVED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    (gen_random_uuid(), i8, alice_id, 'INCIDENT_CREATED', NULL, NULL, 'Incident created', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
    (gen_random_uuid(), i8, david_id, 'STATUS_CHANGED', 'ASSIGNED', 'CLOSED', 'Status changed from RESOLVED to CLOSED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

  -- Seed comments for incident i1
  INSERT INTO comments (id, incident_id, author_id, content, author_role, created_at, updated_at)
  VALUES
    (gen_random_uuid(), i1, carol_id, 'Thank you for reporting this. We will dispatch maintenance staff immediately.', 'MANAGER', NOW() - INTERVAL '3 days' + INTERVAL '30 minutes', NOW() - INTERVAL '3 days' + INTERVAL '30 minutes'),
    (gen_random_uuid(), i1, bob_id, 'On site now. Inspected boiler — pressure valve has failed. Replacement part ordered, ETA tomorrow morning.', 'AGENT', NOW() - INTERVAL '1 day' + INTERVAL '2 hours', NOW() - INTERVAL '1 day' + INTERVAL '2 hours'),
    (gen_random_uuid(), i1, alice_id, 'Is there any temporary heating available? The flat is getting very cold.', 'USER', NOW() - INTERVAL '1 day' + INTERVAL '3 hours', NOW() - INTERVAL '1 day' + INTERVAL '3 hours'),
    (gen_random_uuid(), i1, bob_id, 'Portable electric heaters have been arranged. Someone will deliver them within the hour.', 'AGENT', NOW() - INTERVAL '1 day' + INTERVAL '3 hours' + INTERVAL '20 minutes', NOW() - INTERVAL '1 day' + INTERVAL '3 hours' + INTERVAL '20 minutes');

END $$;
