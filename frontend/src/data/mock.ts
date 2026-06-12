import type {
  User,
  Incident,
  Comment,
  AuditLog,
  DashboardSummary,
  StatusCount,
  CategoryCount,
  PriorityCount,
} from '@/types';

// --- Users ---

export const users: User[] = [
  { id: 'u1', name: 'Alice Chen', email: 'alice@tenant.io', role: 'USER' },
  { id: 'u2', name: 'Bob Torres', email: 'bob@property.io', role: 'AGENT' },
  { id: 'u3', name: 'Carol Perry', email: 'carol@property.io', role: 'MANAGER' },
  { id: 'u4', name: 'David Kim', email: 'david@property.io', role: 'AGENT' },
  { id: 'u5', name: 'Emma Wright', email: 'emma@tenant.io', role: 'USER' },
  { id: 'u6', name: 'Faisal Ahmed', email: 'faisal@tenant.io', role: 'USER' },
];

export const currentUser: User = users[2]; // Carol Perry, Manager

// --- Incidents ---

export const incidents: Incident[] = [
  {
    id: 'INC-001',
    title: 'Heating not working in Flat B204',
    description: 'Radiators are cold in all rooms. Thermostat shows no response. Temperature dropping rapidly. Resident reports it has been off since last night.',
    category: 'MAINTENANCE',
    priority: 'CRITICAL',
    status: 'IN_PROGRESS',
    createdBy: users[0],
    assignedTo: users[1],
    createdAt: '2025-01-15T09:22:00Z',
    updatedAt: '2025-01-15T14:30:00Z',
  },
  {
    id: 'INC-002',
    title: 'Water leak under bathroom sink',
    description: 'Constant dripping from pipe joint under sink. Water pooling on floor tiles. Have placed a bucket but it fills within hours.',
    category: 'MAINTENANCE',
    priority: 'HIGH',
    status: 'ASSIGNED',
    createdBy: users[4],
    assignedTo: users[3],
    createdAt: '2025-01-15T11:45:00Z',
    updatedAt: '2025-01-15T12:10:00Z',
  },
  {
    id: 'INC-003',
    title: 'Broken front door lock',
    description: 'Key turns but deadbolt does not engage. Door cannot be locked securely. This is the main entrance to the building.',
    category: 'SAFETY',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    createdBy: users[5],
    assignedTo: users[1],
    createdAt: '2025-01-14T16:30:00Z',
    updatedAt: '2025-01-15T10:00:00Z',
  },
  {
    id: 'INC-004',
    title: 'Noise complaint after midnight',
    description: 'Loud music from Flat C305 between 1am and 3am, three nights running. Other residents on floor 3 also affected.',
    category: 'NOISE',
    priority: 'MEDIUM',
    status: 'NEW',
    createdBy: users[0],
    createdAt: '2025-01-15T08:15:00Z',
    updatedAt: '2025-01-15T08:15:00Z',
  },
  {
    id: 'INC-005',
    title: 'Wi-Fi unavailable in Room C312',
    description: 'No wireless signal detected since yesterday evening. Router light is off. Other rooms on same floor also appear to be affected.',
    category: 'INTERNET',
    priority: 'MEDIUM',
    status: 'ASSIGNED',
    createdBy: users[4],
    assignedTo: users[3],
    createdAt: '2025-01-14T20:00:00Z',
    updatedAt: '2025-01-15T09:00:00Z',
  },
  {
    id: 'INC-006',
    title: 'Deposit deduction dispute',
    description: 'Charged £150 for carpet cleaning but carpet was professionally cleaned before checkout. Have receipt as evidence.',
    category: 'DEPOSIT',
    priority: 'MEDIUM',
    status: 'NEW',
    createdBy: users[5],
    createdAt: '2025-01-13T14:00:00Z',
    updatedAt: '2025-01-13T14:00:00Z',
  },
  {
    id: 'INC-007',
    title: 'Mould appearing near bedroom window',
    description: 'Black mould patches spreading on wall beside window frame. Getting worse each week. Affecting air quality.',
    category: 'MAINTENANCE',
    priority: 'HIGH',
    status: 'RESOLVED',
    createdBy: users[0],
    assignedTo: users[1],
    createdAt: '2025-01-10T10:00:00Z',
    updatedAt: '2025-01-14T16:00:00Z',
  },
  {
    id: 'INC-008',
    title: 'Washing machine broken in shared laundry',
    description: 'Machine stops mid-cycle with error code E3. Clothes stuck inside. Multiple residents affected.',
    category: 'MAINTENANCE',
    priority: 'MEDIUM',
    status: 'CLOSED',
    createdBy: users[4],
    assignedTo: users[3],
    createdAt: '2025-01-08T09:00:00Z',
    updatedAt: '2025-01-12T11:00:00Z',
  },
  {
    id: 'INC-009',
    title: 'Communal hallway dirty',
    description: 'Ground floor hallway has not been cleaned for over two weeks. Visible dirt and scuff marks throughout.',
    category: 'CLEANING',
    priority: 'LOW',
    status: 'ASSIGNED',
    createdBy: users[5],
    assignedTo: users[3],
    createdAt: '2025-01-14T11:00:00Z',
    updatedAt: '2025-01-14T15:00:00Z',
  },
  {
    id: 'INC-010',
    title: 'Key fob not working for gym access',
    description: 'Key fob stopped working for the building gym. Tried multiple times at different readers. Other doors work fine.',
    category: 'ACCESS',
    priority: 'LOW',
    status: 'NEW',
    createdBy: users[0],
    createdAt: '2025-01-15T07:30:00Z',
    updatedAt: '2025-01-15T07:30:00Z',
  },
  {
    id: 'INC-011',
    title: 'Electricity bill discrepancy',
    description: 'January electricity charge is 3x higher than last month despite no change in usage patterns. Requesting review.',
    category: 'BILLING',
    priority: 'MEDIUM',
    status: 'NEW',
    createdBy: users[4],
    createdAt: '2025-01-15T13:00:00Z',
    updatedAt: '2025-01-15T13:00:00Z',
  },
  {
    id: 'INC-012',
    title: 'Fire alarm testing notification',
    description: 'Request for advance notice before scheduled fire alarm testing. Previous test was unannounced and caused disruption.',
    category: 'SAFETY',
    priority: 'LOW',
    status: 'CLOSED',
    createdBy: users[5],
    assignedTo: users[1],
    createdAt: '2025-01-05T09:00:00Z',
    updatedAt: '2025-01-07T10:00:00Z',
  },
];

// --- Comments for INC-001 ---

export const commentsForCase: Comment[] = [
  {
    id: 'c1',
    content: 'Thank you for reporting this. We will dispatch maintenance staff to inspect the boiler system immediately.',
    author: users[2],
    authorRole: 'MANAGER',
    createdAt: '2025-01-15T09:45:00Z',
  },
  {
    id: 'c2',
    content: 'On site now. Inspected boiler — pressure valve has failed. Replacement part ordered from supplier, ETA tomorrow morning.',
    author: users[1],
    authorRole: 'AGENT',
    createdAt: '2025-01-15T11:30:00Z',
  },
  {
    id: 'c3',
    content: 'Is there any temporary heating solution available? The flat is getting very cold.',
    author: users[0],
    authorRole: 'USER',
    createdAt: '2025-01-15T13:00:00Z',
  },
  {
    id: 'c4',
    content: 'We are arranging portable electric heaters for affected flats. Someone will deliver them within the hour.',
    author: users[1],
    authorRole: 'AGENT',
    createdAt: '2025-01-15T13:20:00Z',
  },
];

// --- Audit logs for INC-001 ---

export const auditLogsForCase: AuditLog[] = [
  {
    id: 'a1',
    action: 'INCIDENT_CREATED',
    actor: users[0],
    message: 'Case created',
    createdAt: '2025-01-15T09:22:00Z',
  },
  {
    id: 'a2',
    action: 'INCIDENT_ASSIGNED',
    actor: users[2],
    oldValue: 'Unassigned',
    newValue: 'Bob Torres',
    message: 'Assigned to Bob Torres',
    createdAt: '2025-01-15T09:40:00Z',
  },
  {
    id: 'a3',
    action: 'STATUS_CHANGED',
    actor: users[2],
    oldValue: 'NEW',
    newValue: 'ASSIGNED',
    message: 'Status changed from NEW to ASSIGNED',
    createdAt: '2025-01-15T09:40:00Z',
  },
  {
    id: 'a4',
    action: 'COMMENT_ADDED',
    actor: users[2],
    message: 'Comment added',
    createdAt: '2025-01-15T09:45:00Z',
  },
  {
    id: 'a5',
    action: 'STATUS_CHANGED',
    actor: users[1],
    oldValue: 'ASSIGNED',
    newValue: 'IN_PROGRESS',
    message: 'Status changed from ASSIGNED to IN_PROGRESS',
    createdAt: '2025-01-15T11:25:00Z',
  },
  {
    id: 'a6',
    action: 'COMMENT_ADDED',
    actor: users[1],
    message: 'Comment added',
    createdAt: '2025-01-15T11:30:00Z',
  },
];

// --- Dashboard ---

export const dashboardSummary: DashboardSummary = {
  totalCases: 47,
  openCases: 12,
  resolvedToday: 3,
  avgResolutionHours: 18.4,
  criticalOpen: 2,
  assignedToMe: 0,
};

export const statusCounts: StatusCount[] = [
  { status: 'NEW', count: 5 },
  { status: 'ASSIGNED', count: 4 },
  { status: 'IN_PROGRESS', count: 3 },
  { status: 'RESOLVED', count: 8 },
  { status: 'CLOSED', count: 25 },
  { status: 'CANCELLED', count: 2 },
];

export const categoryCounts: CategoryCount[] = [
  { category: 'MAINTENANCE', count: 18 },
  { category: 'SAFETY', count: 5 },
  { category: 'NOISE', count: 7 },
  { category: 'INTERNET', count: 4 },
  { category: 'BILLING', count: 3 },
  { category: 'DEPOSIT', count: 2 },
  { category: 'CLEANING', count: 4 },
  { category: 'ACCESS', count: 2 },
  { category: 'OTHER', count: 2 },
];

export const priorityCounts: PriorityCount[] = [
  { priority: 'CRITICAL', count: 2 },
  { priority: 'HIGH', count: 5 },
  { priority: 'MEDIUM', count: 8 },
  { priority: 'LOW', count: 4 },
];

// --- Recent activity ---

export const recentActivity: AuditLog[] = [
  {
    id: 'ra1',
    action: 'STATUS_CHANGED',
    actor: users[1],
    oldValue: 'ASSIGNED',
    newValue: 'IN_PROGRESS',
    message: 'INC-001: Heating not working in Flat B204',
    createdAt: '2025-01-15T14:30:00Z',
  },
  {
    id: 'ra2',
    action: 'COMMENT_ADDED',
    actor: users[1],
    message: 'INC-001: Added update about portable heaters',
    createdAt: '2025-01-15T13:20:00Z',
  },
  {
    id: 'ra3',
    action: 'INCIDENT_ASSIGNED',
    actor: users[2],
    oldValue: 'Unassigned',
    newValue: 'David Kim',
    message: 'INC-002: Water leak assigned to David Kim',
    createdAt: '2025-01-15T12:10:00Z',
  },
  {
    id: 'ra4',
    action: 'INCIDENT_CREATED',
    actor: users[4],
    message: 'INC-011: Electricity bill discrepancy',
    createdAt: '2025-01-15T13:00:00Z',
  },
  {
    id: 'ra5',
    action: 'STATUS_CHANGED',
    actor: users[1],
    oldValue: 'IN_PROGRESS',
    newValue: 'RESOLVED',
    message: 'INC-007: Mould issue resolved',
    createdAt: '2025-01-14T16:00:00Z',
  },
];

// --- Trend data for charts ---

export const weeklyTrend = [
  { day: 'Mon', created: 4, resolved: 3 },
  { day: 'Tue', created: 6, resolved: 5 },
  { day: 'Wed', created: 3, resolved: 4 },
  { day: 'Thu', created: 7, resolved: 2 },
  { day: 'Fri', created: 5, resolved: 6 },
  { day: 'Sat', created: 2, resolved: 3 },
  { day: 'Sun', created: 1, resolved: 1 },
];
