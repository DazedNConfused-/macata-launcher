module.exports = {
    extends: ['@commitlint/config-conventional'],
    parserPreset: {
        parserOpts: {
            headerPattern: /^(\w+)(?:\((\w+)\))?: (.*) \[macata #(\d+)\]$/,
            headerCorrespondence: ['type', 'scope', 'subject', 'issue'],
        },
    },
    rules: {
        'type-enum': [2, 'always', [
            'feat',
            'fix',
            'chore',
            'refactor',
            'ci',
            'test',
            'revert',
            'perf',
            'wip'
        ] ],
        'header-max-length': [2, 'always', 100],
        'references-empty': [2, 'never']
    },
};