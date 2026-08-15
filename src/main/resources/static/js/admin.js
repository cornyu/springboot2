/* global Vue, axios, ELEMENT */

// 会话过期(401)自动回登录页
axios.interceptors.response.use(function (res) { return res; }, function (err) {
    if (err.response && err.response.status === 401) {
        window.location.href = 'login.html';
    }
    return Promise.reject(err);
});

new Vue({
    el: '#app',
    data: {
        treeData: [],
        treeProps: { children: 'children', label: 'label' },
        selectedNode: null,
        columnConfigs: [],
        columnLoading: false,
        editingNodeId: null,
        editLabel: '',
        addGroupVisible: false,
        addGroupForm: { label: '', parentId: null },
        addTableVisible: false,
        addTableForm: { label: '', tableName: '', parentId: null },
        dbTables: []
    },
    computed: {
        groupOptions: function () {
            var result = [];
            function walk(nodes) {
                nodes.forEach(function (n) {
                    if (n.nodeType === 'GROUP') {
                        result.push({ id: n.id, label: n.label });
                        if (n.children) walk(n.children);
                    }
                });
            }
            walk(this.treeData);
            return result;
        }
    },
    methods: {
        // 加载树
        loadTree: function () {
            var self = this;
            axios.get('/api/admin/tree').then(function (res) {
                self.treeData = res.data.data || [];
            }).catch(function () {
                ELEMENT.Message.error('加载树结构失败');
            });
        },
        // 加载数据库表列表
        loadDbTables: function () {
            var self = this;
            axios.get('/api/db/tables').then(function (res) {
                self.dbTables = res.data.data || [];
            }).catch(function () {});
        },
        // 点击节点
        onNodeClick: function (data) {
            this.selectedNode = data;
            if (data.nodeType === 'TABLE') {
                this.loadColumnConfig(data.id);
            }
        },
        // 加载列配置
        loadColumnConfig: function (nodeId) {
            var self = this;
            self.columnLoading = true;
            axios.get('/api/admin/tree/node/' + nodeId + '/columns').then(function (res) {
                self.columnConfigs = res.data.data || [];
            }).catch(function () {
                self.columnConfigs = [];
            }).finally(function () {
                self.columnLoading = false;
            });
        },
        // 开始编辑
        startEdit: function (data) {
            this.editingNodeId = data.id;
            this.editLabel = data.label;
            this.$nextTick(function () {
                if (this.$refs.editInput) {
                    this.$refs.editInput.focus();
                }
            });
        },
        // 保存编辑
        saveEdit: function (data) {
            if (!this.editLabel || this.editLabel.trim() === '') {
                ELEMENT.Message.warning('名称不能为空');
                return;
            }
            var self = this;
            data.label = this.editLabel.trim();
            axios.put('/api/admin/tree/node', {
                id: data.id,
                parentId: data.parentId,
                label: data.label,
                nodeType: data.nodeType,
                tableName: data.tableName,
                sortOrder: data.sortOrder || 0,
                exposed: data.exposed
            }).then(function () {
                ELEMENT.Message.success('已更新');
                self.editingNodeId = null;
            }).catch(function () {
                ELEMENT.Message.error('更新失败');
                self.editingNodeId = null;
            });
        },
        // 切换显隐
        toggleExposed: function (data) {
            var self = this;
            var newVal = !data.exposed;
            axios.put('/api/admin/tree/node', {
                id: data.id,
                parentId: data.parentId,
                label: data.label,
                nodeType: data.nodeType,
                tableName: data.tableName,
                sortOrder: data.sortOrder || 0,
                exposed: newVal
            }).then(function () {
                data.exposed = newVal;
                ELEMENT.Message.success(newVal ? '已显示' : '已隐藏');
            }).catch(function () {
                ELEMENT.Message.error('操作失败');
            });
        },
        // 确认删除
        confirmDelete: function (data) {
            var self = this;
            var type = data.nodeType === 'GROUP' ? '分组' : '数据表';
            ELEMENT.MessageBox.confirm(
                '确定删除"' + data.label + '"' + type + '吗？' + (data.nodeType === 'GROUP' ? '会同时删除其下所有子节点。' : ''),
                '确认删除',
                { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
            ).then(function () {
                self.deleteNode(data);
            }).catch(function () {});
        },
        deleteNode: function (data) {
            var self = this;
            axios.delete('/api/admin/tree/node/' + data.id).then(function () {
                ELEMENT.Message.success('已删除');
                if (self.selectedNode && self.selectedNode.id === data.id) {
                    self.selectedNode = null;
                    self.columnConfigs = [];
                }
                self.loadTree();
            }).catch(function () {
                ELEMENT.Message.error('删除失败');
            });
        },
        // 拖放后更新父节点
        onNodeDrop: function (draggedNode, targetNode, dropType) {
            var self = this;
            var newParentId = null;
            if (dropType === 'inner') {
                newParentId = targetNode.data.id;
            } else if (targetNode.data.parentId) {
                newParentId = targetNode.data.parentId;
            }
            // 更新拖拽节点的parentId
            axios.put('/api/admin/tree/node', {
                id: draggedNode.data.id,
                parentId: newParentId,
                label: draggedNode.data.label,
                nodeType: draggedNode.data.nodeType,
                tableName: draggedNode.data.tableName,
                exposed: draggedNode.data.exposed
            }).then(function () {
                ELEMENT.Message.success('节点已移动');
                self.loadTree();
            }).catch(function () {
                ELEMENT.Message.error('移动失败，请刷新重试');
                self.loadTree();
            });
        },
        // 新建分组对话框
        showAddGroupDialog: function () {
            this.addGroupForm = { label: '', parentId: null };
            this.addGroupVisible = true;
        },
        // 新建分组
        addGroup: function () {
            if (!this.addGroupForm.label || this.addGroupForm.label.trim() === '') {
                ELEMENT.Message.warning('请输入分组名称');
                return;
            }
            var self = this;
            axios.post('/api/admin/tree/node', {
                label: this.addGroupForm.label.trim(),
                nodeType: 'GROUP',
                parentId: this.addGroupForm.parentId || null,
                exposed: true
            }).then(function () {
                ELEMENT.Message.success('分组已创建');
                self.addGroupVisible = false;
                self.loadTree();
            }).catch(function () {
                ELEMENT.Message.error('创建失败');
            });
        },
        // 添加表对话框
        showAddTableDialog: function () {
            this.addTableForm = { label: '', tableName: '', parentId: null };
            this.loadDbTables();
            this.addTableVisible = true;
        },
        // 添加表
        addTable: function () {
            if (!this.addTableForm.label || this.addTableForm.label.trim() === '') {
                ELEMENT.Message.warning('请输入显示名称');
                return;
            }
            if (!this.addTableForm.tableName) {
                ELEMENT.Message.warning('请选择数据库表');
                return;
            }
            var self = this;
            axios.post('/api/admin/tree/node', {
                label: this.addTableForm.label.trim(),
                nodeType: 'TABLE',
                tableName: this.addTableForm.tableName,
                parentId: this.addTableForm.parentId || null,
                exposed: true
            }).then(function () {
                ELEMENT.Message.success('数据表已添加');
                self.addTableVisible = false;
                self.loadTree();
            }).catch(function () {
                ELEMENT.Message.error('添加失败');
            });
        },
        // 列配置操作
        selectAllColumns: function () {
            this.columnConfigs.forEach(function (c) { c.exposed = true; });
            this.saveColumnConfig();
        },
        deselectAllColumns: function () {
            this.columnConfigs.forEach(function (c) { c.exposed = false; });
            this.saveColumnConfig();
        },
        saveColumnConfig: function () {
            if (!this.selectedNode || this.selectedNode.nodeType !== 'TABLE') return;
            var self = this;
            axios.put('/api/admin/tree/node/' + this.selectedNode.id + '/columns', this.columnConfigs).then(function () {
                // 成功，不弹提示以免频繁打扰
            }).catch(function () {
                ELEMENT.Message.error('保存列配置失败');
            });
        },
        // 跳转到数据浏览器
        goToBrowser: function () {
            window.location.href = 'index.html';
        },
        // 退出登录
        logout: function () {
            axios.post('/logout').finally(function () {
                window.location.href = 'login.html?logout=1';
            });
        }
    },
    mounted: function () {
        this.loadTree();
    }
});
