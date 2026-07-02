/* global Vue, axios, ELEMENT */

var API_BASE = window.location.origin;
axios.defaults.baseURL = API_BASE;

new Vue({
    el: '#app',
    data: {
        treeData: [],
        selectedTable: null,      // 实际数据库表名
        selectedTableLabel: '',   // 显示名称
        columns: [],
        selectedColumns: [],
        whereConditions: [{ field: '', operator: '=', value: '' }],
        orderBy: '',
        order: 'ASC',
        resultColumns: [],
        resultRows: [],
        total: 0,
        loading: false,
        queryLoading: false,
        queryTime: 0,
        currentPage: 1,
        pageSize: 100,
        dialogVisible: false,
        treeProps: { children: 'children', label: 'label' }
    },
    computed: {
        apiEndpoint: function () {
            return API_BASE + '/api/db/query';
        },
        apiRequestBody: function () {
            var req = {
                tableName: this.selectedTable,
                columns: this.selectedColumns.length > 0 ? this.selectedColumns : ['*'],
                whereList: this.whereConditions.filter(function (c) { return c.field && c.value; }),
                orderBy: this.orderBy || undefined,
                order: this.order,
                limit: this.pageSize,
                offset: (this.currentPage - 1) * this.pageSize
            };
            return JSON.stringify(req, null, 2);
        },
        apiCurl: function () {
            var body = JSON.stringify(JSON.parse(this.apiRequestBody));
            return 'curl -X POST \\\n  ' + this.apiEndpoint + ' \\\n  -H "Content-Type: application/json" \\\n  -d \'' + body + '\'';
        },
        apiResponseBody: function () {
            var sampleRows = this.resultRows.slice(0, 3);
            return JSON.stringify({
                code: 200,
                message: 'success',
                data: {
                    columns: this.resultColumns,
                    rows: sampleRows,
                    total: this.total
                }
            }, null, 2);
        }
    },
    methods: {
        // 加载树
        loadTree: function () {
            var self = this;
            self.loading = true;
            axios.get('/api/db/tree').then(function (res) {
                self.treeData = res.data.data || [];
            }).catch(function () {
                ELEMENT.Message.error('加载树结构失败');
            }).finally(function () {
                self.loading = false;
            });
        },
        // 点击树节点（只有TABLE类型才触发加载）
        onTableClick: function (data) {
            if (data.nodeType !== 'TABLE' || !data.tableName) return;
            var self = this;
            if (data.tableName === self.selectedTable) return;
            self.selectedTable = data.tableName;
            self.selectedTableLabel = data.label;
            self.columns = [];
            self.selectedColumns = [];
            self.whereConditions = [{ field: '', operator: '=', value: '' }];
            self.orderBy = '';
            self.order = 'ASC';
            self.resultColumns = [];
            self.resultRows = [];
            self.total = 0;
            self.queryTime = 0;
            self.currentPage = 1;
            self.loading = true;
            axios.get('/api/db/columns', { params: { table: data.tableName } }).then(function (res) {
                if (res.data.code === 200) {
                    self.columns = res.data.data;
                    self.selectedColumns = self.columns.map(function (c) { return c.name; });
                } else {
                    ELEMENT.Message.error(res.data.message || '加载字段失败');
                }
            }).catch(function (err) {
                ELEMENT.Message.error('请求失败: ' + (err.message || '网络错误'));
            }).finally(function () {
                self.loading = false;
            });
        },
        // 全选字段
        selectAllFields: function () {
            this.selectedColumns = this.columns.map(function (c) { return c.name; });
        },
        // 添加条件
        addCondition: function () {
            this.whereConditions.push({ field: '', operator: '=', value: '' });
        },
        // 移除条件
        removeCondition: function (index) {
            if (this.whereConditions.length > 1) {
                this.whereConditions.splice(index, 1);
            }
        },
        // 执行查询
        executeQuery: function () {
            var self = this;
            if (!self.selectedTable) {
                ELEMENT.Message.warning('请先选择数据表');
                return;
            }
            self.queryLoading = true;
            var startTime = Date.now();
            var params = {
                tableName: self.selectedTable,
                columns: self.selectedColumns.length > 0 ? self.selectedColumns : null,
                whereList: self.whereConditions.filter(function (c) { return c.field && c.value; }),
                orderBy: self.orderBy || undefined,
                order: self.order,
                limit: self.pageSize,
                offset: (self.currentPage - 1) * self.pageSize
            };
            axios.post('/api/db/query', params).then(function (res) {
                if (res.data.code === 200) {
                    var data = res.data.data;
                    self.resultColumns = data.columns || [];
                    self.resultRows = data.rows || [];
                    self.total = data.total || 0;
                    self.queryTime = Date.now() - startTime;
                } else {
                    ELEMENT.Message.error(res.data.message || '查询失败');
                }
            }).catch(function (err) {
                if (err.response && err.response.data) {
                    ELEMENT.Message.error(err.response.data.message || '查询失败');
                } else {
                    ELEMENT.Message.error('查询失败: ' + (err.message || '网络错误'));
                }
            }).finally(function () {
                self.queryLoading = false;
            });
        },
        // 重置
        resetQuery: function () {
            this.selectedColumns = this.columns.map(function (c) { return c.name; });
            this.whereConditions = [{ field: '', operator: '=', value: '' }];
            this.orderBy = '';
            this.order = 'ASC';
            this.currentPage = 1;
        },
        // 分页
        onPageSizeChange: function (size) {
            this.pageSize = size;
            this.currentPage = 1;
            this.executeQuery();
        },
        onPageChange: function (page) {
            this.currentPage = page;
            this.executeQuery();
        },
        // 导出CSV
        exportCsv: function () {
            if (!this.resultRows.length) {
                ELEMENT.Message.warning('没有数据可导出');
                return;
            }
            var self = this;
            var headers = self.resultColumns.map(function (c) {
                return '"' + c.replace(/"/g, '""') + '"';
            }).join(',');
            var rows = self.resultRows.map(function (row) {
                return self.resultColumns.map(function (col) {
                    var val = row[col];
                    if (val === null || val === undefined) return '';
                    return '"' + String(val).replace(/"/g, '""') + '"';
                }).join(',');
            }).join('\n');
            var csv = '﻿' + headers + '\n' + rows;
            var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
            var link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = self.selectedTable + '_' + new Date().toISOString().slice(0, 10) + '.csv';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(link.href);
            ELEMENT.Message.success('导出成功');
        },
        // 生成接口信息
        showGenerateDialog: function () {
            this.dialogVisible = true;
        },
        copyApiInfo: function () {
            var text = '接口地址: POST ' + this.apiEndpoint + '\n\n'
                + '请求参数:\n' + this.apiRequestBody + '\n\n'
                + 'cURL:\n' + this.apiCurl + '\n\n'
                + '响应示例:\n' + this.apiResponseBody;
            if (navigator.clipboard) {
                navigator.clipboard.writeText(text).then(function () {
                    ELEMENT.Message.success('已复制到剪贴板');
                }).catch(function () {
                    ELEMENT.Message.warning('复制失败，请手动复制');
                });
            } else {
                ELEMENT.Message.warning('复制失败，请手动复制');
            }
        }
    },
    goToAdmin: function () {
        window.location.href = 'admin.html';
    },
    filterNode: function (value, data) {
        if (!value) return true;
        return data.label.indexOf(value) !== -1;
    },
    mounted: function () {
        this.loadTree();
    }
});
