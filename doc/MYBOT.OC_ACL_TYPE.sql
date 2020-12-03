Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'columns', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'menus', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'acls', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'aclTypes', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'types', '4', '1', '1', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'codes', '4', '2', '2', 
    '2', '2', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'groups', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'users', '4', '2', '2', 
    '2', '1', '1');
Insert into OC_ACL_TYPE
   (ACL_ID, TYPE_ID, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, OWNER_GROUP_PERMIT)
 Values
   ('default', 'events', '4', '2', '1', 
    '1', '1', '1');
COMMIT;
