/* global _:false, $j:false, Backbone:false, baseUrl:false */

window.SS = typeof window.SS === 'object' ? window.SS : {};

(function() {

  var DetailsSelectFilterView = window.SS.DetailsFilterView.extend({
    template: '#selectFilterTemplate',
    itemTemplate: '#selectFilterItemTemplate',


    events: function() {
      return {
        'change .choices input[type=checkbox]': 'addToSelection',
        'change .selection input[type=checkbox]': 'removeFromSelection'
      };
    },


    render: function() {
      window.SS.DetailsFilterView.prototype.render.apply(this, arguments);
      this.updateLists();
    },


    renderList: function(collection, selector, checked) {
      var that = this,
          container = this.$(selector),
          t = _.template($j(this.itemTemplate).html());

      container.empty().toggle(collection.length > 0);
      collection.each(function(item) {
        container.append(t(_.extend(
            {
              item: item.toJSON(),
              checked: checked
            }, that.model.toJSON())));
      });
    },


    updateLists: function() {
      this.renderList(this.options.filterView.selection, '.selection', true);
      this.renderList(this.options.filterView.choices, '.choices', false);
    },


    addToSelection: function(e) {
      var id = $j(e.target).val(),
          model = this.options.filterView.choices.findWhere({ id: id });

      this.options.filterView.selection.add(model);
      this.options.filterView.choices.remove(model);

      this.updateValue();
      this.updateLists();
    },


    removeFromSelection: function(e) {
      var id = $j(e.target).val(),
          model = this.options.filterView.selection.findWhere({ id: id });

      this.options.filterView.choices.add(model);
      this.options.filterView.selection.remove(model);

      this.updateValue();
      this.updateLists();
    },


    updateValue: function() {
      this.model.set('value', this.options.filterView.selection.map(function(m) {
        return m.get('id');
      }));
    },


    serializeData: function() {
      return _.extend({}, this.model.toJSON(), {
        selection: this.options.filterView.selection.toJSON(),
        choices: this.options.filterView.choices.toJSON()
      });
    }
  });



  var SelectFilterView = window.SS.BaseFilterView.extend({
    className: 'navigator-filter',


    initialize: function() {
      window.SS.BaseFilterView.prototype.initialize.call(this, {
        detailsView: DetailsSelectFilterView
      });


      this.selection = new Backbone.Collection([], { comparator: 'index' });

      var index = 0;
      this.choices = new Backbone.Collection(
          _.map(this.model.get('choices'), function(value, key) {
            return new Backbone.Model({ id: key, text: value, index: index++ });
          }), { comparator: 'index' }
      );
    },


    renderValue: function() {
      var value = this.selection.map(function(item) {
            return item.get('text');
          });

      return this.isDefaultValue() ? 'All' : value.join(', ');
    },


    isDefaultValue: function() {
      return this.selection.length === 0 || this.choices.length === 0;
    }

  });



  /*
   * Export public classes
   */

  _.extend(window.SS, {
    DetailsSelectFilterView: DetailsSelectFilterView,
    SelectFilterView: SelectFilterView
  });

})();
